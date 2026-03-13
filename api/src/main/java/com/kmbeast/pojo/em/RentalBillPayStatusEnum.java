package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账单支付状态
 */
@Getter
@AllArgsConstructor
public enum RentalBillPayStatusEnum {

    STATUS_1(1, "待支付"),
    STATUS_2(2, "已支付"),
    STATUS_3(3, "已取消");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (RentalBillPayStatusEnum value : values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "未知";
    }
}
