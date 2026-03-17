#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将《中国租房信息数据集.csv》批量导入到当前项目的 house 表。

使用前请确认：
1. 已安装 pymysql: pip install pymysql
2. 数据库可连接
3. 用户/房东 test001 已存在，且 landlord.is_audit = 1

脚本默认策略：
- 房东统一绑定到 test001
- 押金方式默认使用“押一付三”(deposit_method_id=2)
- 装修默认使用“简装”(fitment_status_id=2)
- 租赁方式默认使用“整租”(rental_type=1)
- 无法可靠推断的朝向保留为 NULL
- 无法找到区级 area 时回退到 city 对应 area
- 若 community 不存在，会自动创建
- 默认复用库里第一条 house 的 cover/covers 作为占位图片

建议首次执行：
- 先把 DRY_RUN = True
- 看打印的统计结果和前几条预览
- 确认无误后再改成 False 真正写库
"""

from __future__ import annotations

import csv
import json
import re
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Tuple

import pymysql
from pymysql.cursors import DictCursor


BASE_DIR = Path(__file__).resolve().parents[1]
CSV_PATH = BASE_DIR / "中国租房信息数据集.csv"

DB_CONFIG = {
    "host": "127.0.0.1",
    "port": 3306,
    "user": "root",
    "password": "root",
    "database": "house-rental",
    "charset": "utf8mb4",
    "cursorclass": DictCursor,
    "autocommit": False,
}

LANDLORD_ACCOUNT = "test001"
DRY_RUN = True
MAX_ROWS: Optional[int] = None
BATCH_SIZE = 500

DEFAULT_DEPOSIT_METHOD_ID = 2
DEFAULT_FITMENT_STATUS_ID = 2
DEFAULT_RENTAL_TYPE = 1
DEFAULT_STATUS = 1
DEFAULT_IS_SUBWAY = 0


FACILITY_ORDER = [
    "冰箱",
    "洗衣机",
    "热水器",
    "宽带",
    "沙发",
    "油烟机",
    "燃气灶",
    "可做饭",
    "电视",
    "空调",
    "衣柜",
    "床",
    "卫生间",
    "智能门锁",
    "阳台",
    "暖气",
    "橱柜",
]

CSV_TO_FACILITY = {
    "是否有冰箱": "冰箱",
    "是否有洗衣机": "洗衣机",
    "是否有热水器": "热水器",
    "是否有宽带": "宽带",
    "是否有沙发": "沙发",
    "是否有电视": "电视",
    "是否有空调": "空调",
    "是否有衣柜": "衣柜",
    "是否有床": "床",
    "是否有阳台": "阳台",
    "是否有暖气": "暖气",
}

SUFFIX_PATTERN = re.compile(r"(特别行政区|自治区|自治州|地区|盟|省|市|区|县)$")


@dataclass
class AreaNode:
    id: int
    parent_id: int
    name: str
    normalized_name: str


def normalize_name(text: str) -> str:
    value = (text or "").strip()
    value = value.replace(" ", "")
    if not value:
        return ""
    while True:
        new_value = SUFFIX_PATTERN.sub("", value)
        if new_value == value:
            break
        value = new_value
    return value


def parse_int(value: str) -> Optional[int]:
    text = (value or "").strip()
    if not text:
        return None
    try:
        return int(float(text))
    except ValueError:
        return None


def parse_decimal(value: str) -> Optional[Decimal]:
    text = (value or "").strip()
    if not text:
        return None
    try:
        return Decimal(text).quantize(Decimal("0.01"))
    except (InvalidOperation, ValueError):
        return None


def parse_float(value: str) -> Optional[float]:
    text = (value or "").strip()
    if not text:
        return None
    try:
        return float(text)
    except ValueError:
        return None


def parse_bool01(value: str) -> bool:
    return str(value).strip() in {"1", "true", "True", "TRUE", "是"}


def build_direction_id(raw_direction: str) -> Optional[int]:
    text = (raw_direction or "").strip()
    if not text:
        return None
    mapping = {
        "东": 1,
        "西": 2,
        "南": 3,
        "北": 4,
        "坐北朝南": 5,
        "南北通透": 6,
    }
    for key, value in mapping.items():
        if key in text:
            return value
    if "南北" in text:
        return 6
    return None


def build_house_type_id(community: str, address: str) -> int:
    text = f"{community}{address}"
    if "公寓" in text:
        return 1
    if "别墅" in text:
        return 2
    if "自建房" in text:
        return 7
    return 6


def build_sized_id(room_count: Optional[int], hall_count: Optional[int]) -> int:
    room = room_count or 0
    hall = hall_count or 0
    if room <= 0:
        return 4
    if room == 1 and hall == 0:
        return 4
    if room == 1:
        return 1
    if room == 2:
        return 2
    if room == 3:
        return 3
    return 5


def build_floor_label(current_floor: Optional[int], total_floor: Optional[int]) -> str:
    if not current_floor or not total_floor or total_floor <= 0:
        return "中"
    ratio = current_floor / total_floor
    if ratio <= 0.33:
        return "低"
    if ratio <= 0.66:
        return "中"
    return "高"


def build_living_facilities(row: Dict[str, str]) -> str:
    selected_map = {label: False for label in FACILITY_ORDER}
    for csv_column, label in CSV_TO_FACILITY.items():
        selected_map[label] = parse_bool01(row.get(csv_column, "0"))

    has_gas = parse_bool01(row.get("是否有燃气", "0"))
    selected_map["燃气灶"] = has_gas
    selected_map["可做饭"] = has_gas
    selected_map["油烟机"] = has_gas
    selected_map["橱柜"] = has_gas
    selected_map["卫生间"] = (parse_int(row.get("卫", "")) or 0) > 0
    selected_map["智能门锁"] = False

    payload = [{"label": label, "selected": bool(selected_map[label])} for label in FACILITY_ORDER]
    return json.dumps(payload, ensure_ascii=False)


def build_house_name(row: Dict[str, str]) -> str:
    community = (row.get("小区") or "未知小区").strip()
    room = parse_int(row.get("室", "")) or 0
    hall = parse_int(row.get("厅", "")) or 0
    rent = parse_decimal(row.get("价格", "")) or Decimal("0.00")
    rent_text = f"{rent:.2f}".rstrip("0").rstrip(".")
    name = f"{community}{room}室{hall}厅 {rent_text}元/月"
    return name[:100]


def html_escape(text: str) -> str:
    return (
        (text or "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
    )


def build_detail(row: Dict[str, str]) -> str:
    city = row.get("城市", "").strip()
    district = row.get("区", "").strip()
    community = row.get("小区", "").strip()
    address = row.get("详细地址", "").strip()
    website = row.get("租房网站名称", "").strip()
    link = row.get("link", "").strip()
    price = row.get("价格", "").strip()
    room = row.get("室", "").strip()
    hall = row.get("厅", "").strip()
    bath = row.get("卫", "").strip()
    size = row.get("面积", "").strip()
    floor = row.get("所属楼层", "").strip()
    total_floor = row.get("总楼层", "").strip()
    orientation = row.get("朝向", "").strip() or "未提供"
    school_distance = row.get("最近学校距离", "").strip() or "未提供"
    school_count = row.get("周边学校个数", "").strip() or "未提供"
    hospital_distance = row.get("最近医院距离", "").strip() or "未提供"
    hospital_count = row.get("周边医院个数", "").strip() or "未提供"

    blocks = [
        f"<p>房源位于 {html_escape(city)}{html_escape(district)}，小区为 {html_escape(community)}，地址：{html_escape(address)}。</p>",
        f"<p>{html_escape(room)}室{html_escape(hall)}厅{html_escape(bath)}卫，建筑面积 {html_escape(size)}㎡，租金 {html_escape(price)} 元/月，楼层 {html_escape(floor)}/{html_escape(total_floor)}，朝向 {html_escape(orientation)}。</p>",
        f"<p>周边配套：最近学校约 {html_escape(school_distance)} 米，周边学校 {html_escape(school_count)} 个；最近医院约 {html_escape(hospital_distance)} 米，周边医院 {html_escape(hospital_count)} 个。</p>",
    ]
    return "".join(blocks)


def load_csv_rows(csv_path: Path) -> List[Dict[str, str]]:
    with csv_path.open("r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        rows = list(reader)
    if MAX_ROWS:
        return rows[:MAX_ROWS]
    return rows


def fetch_landlord_id(cursor, account: str) -> int:
    cursor.execute(
        """
        SELECT l.id
        FROM landlord l
        JOIN user u ON u.id = l.user_id
        WHERE l.is_audit = 1
          AND (u.account = %s OR u.username = %s)
        LIMIT 1
        """,
        (account, account),
    )
    row = cursor.fetchone()
    if not row:
        raise RuntimeError(f"未找到已审核房东：{account}")
    return int(row["id"])


def fetch_default_media(cursor) -> Tuple[str, str]:
    cursor.execute(
        """
        SELECT cover, covers
        FROM house
        WHERE cover IS NOT NULL AND cover <> ''
        ORDER BY id ASC
        LIMIT 1
        """
    )
    row = cursor.fetchone()
    if not row:
        return "", ""
    cover = row.get("cover") or ""
    covers = row.get("covers") or cover
    return cover, covers


def load_area_index(cursor) -> Tuple[Dict[int, AreaNode], Dict[str, List[AreaNode]]]:
    cursor.execute("SELECT id, parent_id, name FROM area")
    rows = cursor.fetchall()
    by_id: Dict[int, AreaNode] = {}
    by_name: Dict[str, List[AreaNode]] = {}
    for row in rows:
        node = AreaNode(
            id=int(row["id"]),
            parent_id=int(row["parent_id"] or 0),
            name=row["name"],
            normalized_name=normalize_name(row["name"]),
        )
        by_id[node.id] = node
        by_name.setdefault(node.normalized_name, []).append(node)
    return by_id, by_name


def has_ancestor_name(node: AreaNode, target_name: str, area_by_id: Dict[int, AreaNode]) -> bool:
    parent_id = node.parent_id
    visited = set()
    while parent_id and parent_id not in visited:
        visited.add(parent_id)
        parent = area_by_id.get(parent_id)
        if not parent:
            return False
        if parent.normalized_name == target_name:
            return True
        parent_id = parent.parent_id
    return False


def resolve_area_id(city: str, district: str, area_by_id: Dict[int, AreaNode], area_by_name: Dict[str, List[AreaNode]]) -> Optional[int]:
    city_key = normalize_name(city)
    district_key = normalize_name(district)

    city_candidates = area_by_name.get(city_key, [])
    district_candidates = area_by_name.get(district_key, [])

    if district_candidates and city_key:
        for node in district_candidates:
            if has_ancestor_name(node, city_key, area_by_id):
                return node.id

    if district_candidates:
        return district_candidates[0].id

    if city_candidates:
        return city_candidates[0].id

    return None


def load_existing_communities(cursor) -> Dict[Tuple[int, str], int]:
    cursor.execute("SELECT id, area_id, name FROM community")
    rows = cursor.fetchall()
    community_map: Dict[Tuple[int, str], int] = {}
    for row in rows:
        key = (int(row["area_id"] or 0), (row["name"] or "").strip())
        community_map[key] = int(row["id"])
    return community_map


def ensure_community(
    cursor,
    community_name: str,
    area_id: int,
    community_cache: Dict[Tuple[int, str], int],
    cover: str,
    covers: str,
    dry_run: bool,
) -> int:
    key = (area_id, community_name.strip())
    cached = community_cache.get(key)
    if cached:
        return cached

    if dry_run:
        temp_id = -(len(community_cache) + 1)
        community_cache[key] = temp_id
        return temp_id

    detail = f"<p>{html_escape(community_name)} 位于 area_id={area_id}，由脚本根据租房数据集自动创建。</p>"
    cursor.execute(
        """
        INSERT INTO community(area_id, name, cover, covers, detail, create_time)
        VALUES(%s, %s, %s, %s, %s, %s)
        """,
        (area_id, community_name.strip(), cover or None, covers or None, detail, datetime.now()),
    )
    community_id = int(cursor.lastrowid)
    community_cache[key] = community_id
    return community_id


def load_existing_house_signatures(cursor) -> set:
    cursor.execute("SELECT community_id, name, rent, size_number FROM house")
    rows = cursor.fetchall()
    signatures = set()
    for row in rows:
        signature = (
            int(row["community_id"] or 0),
            (row["name"] or "").strip(),
            str(row["rent"] or ""),
            round(float(row["size_number"] or 0), 2),
        )
        signatures.add(signature)
    return signatures


def build_house_signature(community_id: int, name: str, rent: Decimal, size_number: float) -> Tuple[int, str, str, float]:
    return community_id, name.strip(), f"{rent:.2f}", round(size_number, 2)


def build_house_record(
    row: Dict[str, str],
    landlord_id: int,
    area_id: int,
    community_id: int,
    default_cover: str,
    default_covers: str,
) -> Optional[Dict[str, object]]:
    price = parse_decimal(row.get("价格", ""))
    size_number = parse_float(row.get("面积", ""))
    room_count = parse_int(row.get("室", ""))
    hall_count = parse_int(row.get("厅", ""))
    bath_count = parse_int(row.get("卫", ""))
    city = (row.get("城市") or "").strip()
    district = (row.get("区") or "").strip()
    community = (row.get("小区") or "").strip()
    address = (row.get("详细地址") or "").strip()

    if not community or not city or not district or price is None or size_number is None:
        return None

    name = build_house_name(row)
    direction_id = build_direction_id(row.get("朝向", ""))
    current_floor = parse_int(row.get("所属楼层", ""))
    total_floor = parse_int(row.get("总楼层", ""))
    floor = build_floor_label(current_floor, total_floor)
    living_facilities = build_living_facilities(row)

    return {
        "name": name,
        "landlord_id": landlord_id,
        "area_id": area_id,
        "community_id": community_id,
        "detail": build_detail(row),
        "cover": default_cover or None,
        "covers": default_covers or default_cover or None,
        "type_id": build_house_type_id(community, address),
        "size_number": size_number,
        "direction_id": direction_id,
        "floor": floor,
        "sized_id": build_sized_id(room_count, hall_count),
        "rent": price,
        "deposit_method_id": DEFAULT_DEPOSIT_METHOD_ID,
        "status": DEFAULT_STATUS,
        "is_subway": DEFAULT_IS_SUBWAY,
        "subway_line": None,
        "fitment_status_id": DEFAULT_FITMENT_STATUS_ID,
        "rental_type": DEFAULT_RENTAL_TYPE,
        "living_facilities": living_facilities,
        "create_time": datetime.now(),
        "source_link": row.get("link", "").strip(),
        "bath_count": bath_count or 0,
    }


def insert_house(cursor, payload: Dict[str, object]) -> None:
    cursor.execute(
        """
        INSERT INTO house(
            name, landlord_id, area_id, community_id, detail, cover, covers,
            type_id, size_number, direction_id, floor, sized_id, rent,
            deposit_method_id, status, is_subway, subway_line,
            fitment_status_id, rental_type, living_facilities, create_time
        ) VALUES (
            %s, %s, %s, %s, %s, %s, %s,
            %s, %s, %s, %s, %s, %s,
            %s, %s, %s, %s,
            %s, %s, %s, %s
        )
        """,
        (
            payload["name"],
            payload["landlord_id"],
            payload["area_id"],
            payload["community_id"],
            payload["detail"],
            payload["cover"],
            payload["covers"],
            payload["type_id"],
            payload["size_number"],
            payload["direction_id"],
            payload["floor"],
            payload["sized_id"],
            payload["rent"],
            payload["deposit_method_id"],
            payload["status"],
            payload["is_subway"],
            payload["subway_line"],
            payload["fitment_status_id"],
            payload["rental_type"],
            payload["living_facilities"],
            payload["create_time"],
        ),
    )


def main() -> None:
    if not CSV_PATH.exists():
        raise FileNotFoundError(f"未找到数据集文件: {CSV_PATH}")

    rows = load_csv_rows(CSV_PATH)
    print(f"数据集文件: {CSV_PATH}")
    print(f"原始行数: {len(rows)}")

    connection = pymysql.connect(**DB_CONFIG)
    try:
        with connection.cursor() as cursor:
            landlord_id = fetch_landlord_id(cursor, LANDLORD_ACCOUNT)
            print(f"已绑定房东: {LANDLORD_ACCOUNT} -> landlord_id={landlord_id}")

            default_cover, default_covers = fetch_default_media(cursor)
            area_by_id, area_by_name = load_area_index(cursor)
            community_cache = load_existing_communities(cursor)
            existing_house_signatures = load_existing_house_signatures(cursor)

            batch_count = 0
            inserted_house_count = 0
            inserted_community_count = 0
            skipped_invalid_count = 0
            skipped_duplicate_count = 0
            batch_links = set()
            preview_records = []

            for index, row in enumerate(rows, start=1):
                source_link = (row.get("link") or "").strip()
                if source_link and source_link in batch_links:
                    skipped_duplicate_count += 1
                    continue

                city = (row.get("城市") or "").strip()
                district = (row.get("区") or "").strip()
                community_name = (row.get("小区") or "").strip()
                area_id = resolve_area_id(city, district, area_by_id, area_by_name)
                if not area_id or not community_name:
                    skipped_invalid_count += 1
                    continue

                community_key = (area_id, community_name)
                before_has_community = community_key in community_cache
                community_id = ensure_community(
                    cursor,
                    community_name=community_name,
                    area_id=area_id,
                    community_cache=community_cache,
                    cover=default_cover,
                    covers=default_covers,
                    dry_run=DRY_RUN,
                )
                if not before_has_community:
                    inserted_community_count += 1

                payload = build_house_record(
                    row=row,
                    landlord_id=landlord_id,
                    area_id=area_id,
                    community_id=community_id,
                    default_cover=default_cover,
                    default_covers=default_covers,
                )
                if not payload:
                    skipped_invalid_count += 1
                    continue

                signature = build_house_signature(
                    community_id=community_id,
                    name=payload["name"],
                    rent=payload["rent"],
                    size_number=payload["size_number"],
                )
                if signature in existing_house_signatures:
                    skipped_duplicate_count += 1
                    continue

                if len(preview_records) < 5:
                    preview_records.append(
                        {
                            "name": payload["name"],
                            "city": city,
                            "district": district,
                            "community": community_name,
                            "rent": str(payload["rent"]),
                            "area_id": area_id,
                            "community_id": community_id,
                        }
                    )

                if not DRY_RUN:
                    insert_house(cursor, payload)
                existing_house_signatures.add(signature)
                if source_link:
                    batch_links.add(source_link)
                inserted_house_count += 1
                batch_count += 1

                if not DRY_RUN and batch_count >= BATCH_SIZE:
                    connection.commit()
                    batch_count = 0
                    print(f"已提交 {inserted_house_count} 条 house 记录")

            if not DRY_RUN and batch_count > 0:
                connection.commit()

            print("\n导入预览（前 5 条）:")
            for item in preview_records:
                print(item)

            print("\n导入统计:")
            print(f"- 新增房源: {inserted_house_count}")
            print(f"- 新增小区: {inserted_community_count}")
            print(f"- 跳过无效数据: {skipped_invalid_count}")
            print(f"- 跳过重复数据: {skipped_duplicate_count}")
            print(f"- 运行模式: {'DRY_RUN 仅预览未写库' if DRY_RUN else '已写入数据库'}")

    except Exception:
        connection.rollback()
        raise
    finally:
        connection.close()


if __name__ == "__main__":
    main()
