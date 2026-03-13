package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.entity.PaymentOrder;
import com.kmbeast.pojo.vo.PaymentStartVO;
import com.kmbeast.pojo.vo.RentalBillVO;

import java.util.Map;

/**
 * 支付订单业务
 */
public interface PaymentOrderService extends IService<PaymentOrder> {

    Result<PaymentStartVO> createPagePay(Integer billId);

    String handleNotify(Map<String, String> params);

    Result<RentalBillVO> queryBillStatus(Integer billId);
}
