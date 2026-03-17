package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报修支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum RepairPaymentStatusEnum {

    STATUS_1(1, "待支付"),
    STATUS_2(2, "已支付"),
    STATUS_3(3, "无需支付");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (RepairPaymentStatusEnum value : RepairPaymentStatusEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "未知";
    }
}
