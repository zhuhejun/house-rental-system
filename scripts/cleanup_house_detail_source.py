#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
清理 house.detail 里由数据导入脚本生成的“数据来源：房天下，原始链接：查看原始房源”段落。

使用方式：
1. 安装依赖：pip install pymysql
2. 先保持 DRY_RUN = True 预览
3. 确认无误后改成 False 正式写库
"""

from __future__ import annotations

import re
from typing import List, Tuple

import pymysql
from pymysql.cursors import DictCursor


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

DRY_RUN = True
BATCH_SIZE = 500


SOURCE_BLOCK_RE = re.compile(
    r"<p>\s*数据来源：.*?原始链接：<a .*?>查看原始房源</a>\s*</p>",
    re.IGNORECASE | re.DOTALL,
)


def clean_detail(detail: str) -> str:
    if not detail:
        return detail
    new_detail = SOURCE_BLOCK_RE.sub("", detail)
    return new_detail.strip()


def main() -> None:
    connection = pymysql.connect(**DB_CONFIG)
    try:
        with connection.cursor() as cursor:
            cursor.execute("SELECT id, detail FROM house WHERE detail LIKE '%数据来源：%'")
            rows = cursor.fetchall()

            changed: List[Tuple[int, str]] = []
            for row in rows:
                house_id = int(row["id"])
                original = row.get("detail") or ""
                updated = clean_detail(original)
                if updated != original:
                    changed.append((house_id, updated))

            print(f"匹配到候选房源: {len(rows)}")
            print(f"需要更新的房源: {len(changed)}")
            print(f"运行模式: {'DRY_RUN 仅预览未写库' if DRY_RUN else '已写入数据库'}")

            if changed:
                print("前 5 条待更新 ID:", [item[0] for item in changed[:5]])

            if DRY_RUN:
                return

            total = 0
            for index, (house_id, detail) in enumerate(changed, start=1):
                cursor.execute("UPDATE house SET detail = %s WHERE id = %s", (detail, house_id))
                total += 1
                if index % BATCH_SIZE == 0:
                    connection.commit()
                    print(f"已提交 {total} 条更新")

            if changed:
                connection.commit()
            print(f"清理完成，共更新 {total} 条房源描述")

    except Exception:
        connection.rollback()
        raise
    finally:
        connection.close()


if __name__ == "__main__":
    main()
