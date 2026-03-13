package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付订单持久层
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    PaymentOrder selectPendingByBillId(@Param(value = "rentalBillId") Integer rentalBillId);

    PaymentOrder selectByOutTradeNo(@Param(value = "outTradeNo") String outTradeNo);
}
