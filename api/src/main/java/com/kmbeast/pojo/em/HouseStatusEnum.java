package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋状态枚举
 */
@Getter
@AllArgsConstructor
public enum HouseStatusEnum {

    STATUS_1(1, "待租"),
    STATUS_2(2, "下架"),
    STATUS_3(3, "已出租");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String detail;

    public static String getDetail(Integer type) {
        for (HouseStatusEnum value : HouseStatusEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

}
