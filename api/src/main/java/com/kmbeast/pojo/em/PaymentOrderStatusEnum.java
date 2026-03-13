package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付订单状态
 */
@Getter
@AllArgsConstructor
public enum PaymentOrderStatusEnum {

    STATUS_1(1, "待支付"),
    STATUS_2(2, "支付成功"),
    STATUS_3(3, "已关闭"),
    STATUS_4(4, "支付失败");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (PaymentOrderStatusEnum value : values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "未知";
    }
}
