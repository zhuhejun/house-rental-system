package com.kmbeast.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "payment_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;

    private Integer rentalBillId;

    private Integer repairOrderId;

    private Integer contractId;

    private Integer landlordId;

    private Integer tenantUserId;

    private String subject;

    private String outTradeNo;

    private BigDecimal totalAmount;

    private String payChannel;

    private Integer status;

    private String alipayTradeNo;

    private String notifyContent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
