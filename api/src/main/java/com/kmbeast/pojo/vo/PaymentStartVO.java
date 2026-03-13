package com.kmbeast.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 发起支付返回值
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStartVO {

    private Integer billId;

    private String outTradeNo;

    private BigDecimal totalAmount;

    private String htmlForm;
}
