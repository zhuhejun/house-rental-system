package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账单类型
 */
@Getter
@AllArgsConstructor
public enum RentalBillTypeEnum {

    STATUS_1(1, "押金单"),
    STATUS_2(2, "租金单");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (RentalBillTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "未知";
    }
}
