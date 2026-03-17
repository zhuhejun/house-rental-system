package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报修类型枚举
 */
@Getter
@AllArgsConstructor
public enum RepairTypeEnum {

    TYPE_1(1, "水路问题"),
    TYPE_2(2, "电路问题"),
    TYPE_3(3, "门锁窗户"),
    TYPE_4(4, "家具家电"),
    TYPE_5(5, "渗水漏水"),
    TYPE_6(6, "卫浴厨房"),
    TYPE_7(7, "墙面地面"),
    TYPE_8(8, "其他");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (RepairTypeEnum value : RepairTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

    public static boolean contains(Integer type) {
        for (RepairTypeEnum value : RepairTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}
