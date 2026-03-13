package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 水电费支付方式
 */
@Getter
@AllArgsConstructor
public enum UtilityPaymentModeEnum {

    STATUS_1(1, "自行缴费"),
    STATUS_2(2, "房东结算");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (UtilityPaymentModeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "未知";
    }
}
