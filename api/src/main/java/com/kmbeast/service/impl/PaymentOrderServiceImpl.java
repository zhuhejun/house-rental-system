package com.kmbeast.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.mapper.PaymentOrderMapper;
import com.kmbeast.mapper.RentalBillMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.em.PaymentOrderStatusEnum;
import com.kmbeast.pojo.em.RentalBillPayStatusEnum;
import com.kmbeast.pojo.em.RentalBillTypeEnum;
import com.kmbeast.pojo.em.RoleEnum;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.entity.PaymentOrder;
import com.kmbeast.pojo.entity.RentalBill;
import com.kmbeast.pojo.vo.PaymentStartVO;
import com.kmbeast.pojo.vo.RentalBillVO;
import com.kmbeast.service.PaymentOrderService;
import com.kmbeast.utils.AlipaySupport;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * 支付订单实现
 */
@Service
public class PaymentOrderServiceImpl extends ServiceImpl<PaymentOrderMapper, PaymentOrder> implements PaymentOrderService {

    @Resource
    private RentalBillMapper rentalBillMapper;
    @Resource
    private LandlordMapper landlordMapper;
    @Resource
    private AlipaySupport alipaySupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<PaymentStartVO> createPagePay(Integer billId) {
        AssertUtils.notNull(billId, "账单ID不能为空");
        RentalBill rentalBill = getBillById(billId);
        AssertUtils.equals(rentalBill.getTenantUserId(), LocalThreadHolder.getUserId(), "仅租客本人可发起支付");
        AssertUtils.equals(rentalBill.getPayStatus(), RentalBillPayStatusEnum.STATUS_1.getType(), "当前账单状态无法支付");
        AssertUtils.isTrue(rentalBill.getTotalAmount() != null && rentalBill.getTotalAmount().doubleValue() > 0, "当前账单金额异常");

        PaymentOrder paymentOrder = this.baseMapper.selectPendingByBillId(billId);
        if (paymentOrder == null) {
            paymentOrder = new PaymentOrder();
            paymentOrder.setOrderNo(generateOrderNo());
            paymentOrder.setRentalBillId(rentalBill.getId());
            paymentOrder.setContractId(rentalBill.getContractId());
            paymentOrder.setLandlordId(rentalBill.getLandlordId());
            paymentOrder.setTenantUserId(rentalBill.getTenantUserId());
            paymentOrder.setSubject(buildSubject(rentalBill));
            paymentOrder.setOutTradeNo(generateOutTradeNo());
            paymentOrder.setTotalAmount(rentalBill.getTotalAmount());
            paymentOrder.setPayChannel("ALIPAY");
            paymentOrder.setStatus(PaymentOrderStatusEnum.STATUS_1.getType());
            paymentOrder.setCreateTime(LocalDateTime.now());
            paymentOrder.setUpdateTime(LocalDateTime.now());
            save(paymentOrder);
        }
        String htmlForm = alipaySupport.buildPagePayForm(paymentOrder.getOutTradeNo(), paymentOrder.getTotalAmount(),
                paymentOrder.getSubject(), rentalBill.getRemark(), alipaySupport.buildReturnUrl(rentalBill.getId()));
        PaymentStartVO paymentStartVO = new PaymentStartVO(rentalBill.getId(), paymentOrder.getOutTradeNo(),
                paymentOrder.getTotalAmount(), htmlForm);
        return ApiResult.success(paymentStartVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "failure";
        }
        if (!alipaySupport.verifySignature(params)) {
            return "failure";
        }
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        PaymentOrder paymentOrder = this.baseMapper.selectByOutTradeNo(outTradeNo);
        if (paymentOrder == null) {
            return "failure";
        }
        if (Objects.equals(paymentOrder.getStatus(), PaymentOrderStatusEnum.STATUS_2.getType())) {
            return "success";
        }
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            markPaymentSuccess(paymentOrder, params.get("trade_no"), JSON.toJSONString(params));
            return "success";
        }
        if ("TRADE_CLOSED".equals(tradeStatus)) {
            paymentOrder.setStatus(PaymentOrderStatusEnum.STATUS_3.getType());
            paymentOrder.setNotifyContent(JSON.toJSONString(params));
            paymentOrder.setUpdateTime(LocalDateTime.now());
            updateById(paymentOrder);
            return "success";
        }
        return "failure";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RentalBillVO> queryBillStatus(Integer billId) {
        AssertUtils.notNull(billId, "账单ID不能为空");
        RentalBillVO rentalBillVO = rentalBillMapper.getById(billId);
        AssertUtils.notNull(rentalBillVO, "账单不存在");
        assertBillAccess(rentalBillVO);
        if (Objects.equals(rentalBillVO.getPayStatus(), RentalBillPayStatusEnum.STATUS_2.getType())) {
            return ApiResult.success(rentalBillVO);
        }

        PaymentOrder paymentOrder = this.baseMapper.selectPendingByBillId(billId);
        if (paymentOrder == null) {
            return ApiResult.success(rentalBillVO);
        }
        AlipaySupport.TradeQueryResult queryResult = alipaySupport.queryTrade(paymentOrder.getOutTradeNo());
        if (queryResult.isSuccess()
                && ("TRADE_SUCCESS".equals(queryResult.getTradeStatus())
                || "TRADE_FINISHED".equals(queryResult.getTradeStatus()))) {
            markPaymentSuccess(paymentOrder, queryResult.getAlipayTradeNo(), queryResult.getRawResponse());
            rentalBillVO = rentalBillMapper.getById(billId);
        } else if ("TRADE_CLOSED".equals(queryResult.getTradeStatus())) {
            paymentOrder.setStatus(PaymentOrderStatusEnum.STATUS_3.getType());
            paymentOrder.setNotifyContent(queryResult.getRawResponse());
            paymentOrder.setUpdateTime(LocalDateTime.now());
            updateById(paymentOrder);
        }
        return ApiResult.success(rentalBillVO);
    }

    private void markPaymentSuccess(PaymentOrder paymentOrder, String alipayTradeNo, String notifyContent) {
        paymentOrder.setStatus(PaymentOrderStatusEnum.STATUS_2.getType());
        paymentOrder.setAlipayTradeNo(alipayTradeNo);
        paymentOrder.setNotifyContent(notifyContent);
        paymentOrder.setPaidTime(LocalDateTime.now());
        paymentOrder.setUpdateTime(LocalDateTime.now());
        updateById(paymentOrder);

        RentalBill rentalBill = getBillById(paymentOrder.getRentalBillId());
        if (!Objects.equals(rentalBill.getPayStatus(), RentalBillPayStatusEnum.STATUS_2.getType())) {
            rentalBill.setPayStatus(RentalBillPayStatusEnum.STATUS_2.getType());
            rentalBill.setPaidTime(LocalDateTime.now());
            rentalBill.setUpdateTime(LocalDateTime.now());
            rentalBillMapper.updateById(rentalBill);
        }
    }

    private RentalBill getBillById(Integer billId) {
        RentalBill rentalBill = rentalBillMapper.selectById(billId);
        AssertUtils.notNull(rentalBill, "账单不存在");
        return rentalBill;
    }

    private void assertBillAccess(RentalBillVO rentalBillVO) {
        boolean isAdmin = Objects.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole());
        boolean isTenant = Objects.equals(rentalBillVO.getTenantUserId(), LocalThreadHolder.getUserId());
        boolean isLandlord = Objects.equals(LocalThreadHolder.getUserId(), getLandlordUserId(rentalBillVO.getLandlordId()));
        AssertUtils.isTrue(isAdmin || isTenant || isLandlord, "非法操作");
    }

    private Integer getLandlordUserId(Integer landlordId) {
        if (landlordId == null) {
            return null;
        }
        Landlord landlord = landlordMapper.selectById(landlordId);
        return landlord == null ? null : landlord.getUserId();
    }

    private String buildSubject(RentalBill rentalBill) {
        if (Objects.equals(rentalBill.getBillType(), RentalBillTypeEnum.STATUS_1.getType())) {
            return "租赁合同押金支付-" + rentalBill.getBillNo();
        }
        return "租赁合同租金支付-" + rentalBill.getBillNo();
    }

    private String generateOrderNo() {
        int random = new Random().nextInt(9000) + 1000;
        return "PO" + System.currentTimeMillis() + random;
    }

    private String generateOutTradeNo() {
        int random = new Random().nextInt(9000) + 1000;
        return "AT" + System.currentTimeMillis() + random;
    }
}
