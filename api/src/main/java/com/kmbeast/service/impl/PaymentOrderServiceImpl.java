package com.kmbeast.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.mapper.PaymentOrderMapper;
import com.kmbeast.mapper.RepairOrderMapper;
import com.kmbeast.mapper.RentalBillMapper;
import com.kmbeast.mapper.RentalContractMapper;
import com.kmbeast.mapper.RentalContractStatusMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.em.HouseStatusEnum;
import com.kmbeast.pojo.em.PaymentOrderStatusEnum;
import com.kmbeast.pojo.em.RepairOrderStatusEnum;
import com.kmbeast.pojo.em.RepairPaymentStatusEnum;
import com.kmbeast.pojo.em.RentalBillPayStatusEnum;
import com.kmbeast.pojo.em.RentalBillTypeEnum;
import com.kmbeast.pojo.em.RentalContractStatusEnum;
import com.kmbeast.pojo.em.RoleEnum;
import com.kmbeast.pojo.entity.House;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.entity.PaymentOrder;
import com.kmbeast.pojo.entity.RepairOrder;
import com.kmbeast.pojo.entity.RentalBill;
import com.kmbeast.pojo.entity.RentalContract;
import com.kmbeast.pojo.entity.RentalContractStatus;
import com.kmbeast.pojo.vo.PaymentStartVO;
import com.kmbeast.pojo.vo.RepairOrderVO;
import com.kmbeast.pojo.vo.RentalBillVO;
import com.kmbeast.service.PaymentOrderService;
import com.kmbeast.service.RentalBillService;
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
    private RepairOrderMapper repairOrderMapper;
    @Resource
    private LandlordMapper landlordMapper;
    @Resource
    private RentalContractMapper rentalContractMapper;
    @Resource
    private RentalContractStatusMapper rentalContractStatusMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private RentalBillService rentalBillService;
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
    public Result<PaymentStartVO> createRepairPagePay(Integer repairOrderId) {
        AssertUtils.notNull(repairOrderId, "报修工单ID不能为空");
        RepairOrder repairOrder = getRepairOrderById(repairOrderId);
        AssertUtils.equals(repairOrder.getTenantUserId(), LocalThreadHolder.getUserId(), "仅租客本人可发起支付");
        AssertUtils.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_4.getType(), "当前工单状态无法支付");
        AssertUtils.equals(repairOrder.getPaymentStatus(), RepairPaymentStatusEnum.STATUS_1.getType(), "当前工单无需支付");
        AssertUtils.isTrue(repairOrder.getRepairAmount() != null && repairOrder.getRepairAmount().doubleValue() > 0, "维修费用异常");

        PaymentOrder paymentOrder = this.baseMapper.selectPendingByRepairOrderId(repairOrderId);
        if (paymentOrder == null) {
            paymentOrder = new PaymentOrder();
            paymentOrder.setOrderNo(generateOrderNo());
            paymentOrder.setRepairOrderId(repairOrder.getId());
            paymentOrder.setContractId(repairOrder.getContractId());
            paymentOrder.setLandlordId(repairOrder.getLandlordId());
            paymentOrder.setTenantUserId(repairOrder.getTenantUserId());
            paymentOrder.setSubject("报修工单支付-" + repairOrder.getRepairNo());
            paymentOrder.setOutTradeNo(generateOutTradeNo());
            paymentOrder.setTotalAmount(repairOrder.getRepairAmount());
            paymentOrder.setPayChannel("ALIPAY");
            paymentOrder.setStatus(PaymentOrderStatusEnum.STATUS_1.getType());
            paymentOrder.setCreateTime(LocalDateTime.now());
            paymentOrder.setUpdateTime(LocalDateTime.now());
            save(paymentOrder);
        }
        String htmlForm = alipaySupport.buildPagePayForm(paymentOrder.getOutTradeNo(), paymentOrder.getTotalAmount(),
                paymentOrder.getSubject(), repairOrder.getHandleNote(), alipaySupport.buildRepairReturnUrl(repairOrder.getId()));
        PaymentStartVO paymentStartVO = new PaymentStartVO(repairOrder.getId(), paymentOrder.getOutTradeNo(),
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RepairOrderVO> queryRepairStatus(Integer repairOrderId) {
        AssertUtils.notNull(repairOrderId, "报修工单ID不能为空");
        RepairOrderVO repairOrderVO = repairOrderMapper.getById(repairOrderId);
        AssertUtils.notNull(repairOrderVO, "报修工单不存在");
        assertRepairAccess(repairOrderVO);
        if (Objects.equals(repairOrderVO.getPaymentStatus(), RepairPaymentStatusEnum.STATUS_2.getType())) {
            return ApiResult.success(repairOrderVO);
        }

        PaymentOrder paymentOrder = this.baseMapper.selectPendingByRepairOrderId(repairOrderId);
        if (paymentOrder == null) {
            return ApiResult.success(repairOrderVO);
        }
        AlipaySupport.TradeQueryResult queryResult = alipaySupport.queryTrade(paymentOrder.getOutTradeNo());
        if (queryResult.isSuccess()
                && ("TRADE_SUCCESS".equals(queryResult.getTradeStatus())
                || "TRADE_FINISHED".equals(queryResult.getTradeStatus()))) {
            markPaymentSuccess(paymentOrder, queryResult.getAlipayTradeNo(), queryResult.getRawResponse());
            repairOrderVO = repairOrderMapper.getById(repairOrderId);
        } else if ("TRADE_CLOSED".equals(queryResult.getTradeStatus())) {
            paymentOrder.setStatus(PaymentOrderStatusEnum.STATUS_3.getType());
            paymentOrder.setNotifyContent(queryResult.getRawResponse());
            paymentOrder.setUpdateTime(LocalDateTime.now());
            updateById(paymentOrder);
        }
        return ApiResult.success(repairOrderVO);
    }

    private void markPaymentSuccess(PaymentOrder paymentOrder, String alipayTradeNo, String notifyContent) {
        paymentOrder.setStatus(PaymentOrderStatusEnum.STATUS_2.getType());
        paymentOrder.setAlipayTradeNo(alipayTradeNo);
        paymentOrder.setNotifyContent(notifyContent);
        paymentOrder.setPaidTime(LocalDateTime.now());
        paymentOrder.setUpdateTime(LocalDateTime.now());
        updateById(paymentOrder);

        if (paymentOrder.getRepairOrderId() != null) {
            markRepairPaymentSuccess(paymentOrder);
            return;
        }

        RentalBill rentalBill = getBillById(paymentOrder.getRentalBillId());
        if (!Objects.equals(rentalBill.getPayStatus(), RentalBillPayStatusEnum.STATUS_2.getType())) {
            rentalBill.setPayStatus(RentalBillPayStatusEnum.STATUS_2.getType());
            rentalBill.setPaidTime(LocalDateTime.now());
            rentalBill.setUpdateTime(LocalDateTime.now());
            rentalBillMapper.updateById(rentalBill);
        }

        if (Objects.equals(rentalBill.getBillType(), RentalBillTypeEnum.STATUS_1.getType())) {
            activateContractAfterDeposit(rentalBill, paymentOrder.getTenantUserId());
        }
    }

    private void markRepairPaymentSuccess(PaymentOrder paymentOrder) {
        RepairOrder repairOrder = getRepairOrderById(paymentOrder.getRepairOrderId());
        if (!Objects.equals(repairOrder.getPaymentStatus(), RepairPaymentStatusEnum.STATUS_2.getType())) {
            repairOrder.setPaymentStatus(RepairPaymentStatusEnum.STATUS_2.getType());
            repairOrder.setPaidTime(LocalDateTime.now());
            repairOrder.setUpdateTime(LocalDateTime.now());
            repairOrderMapper.updateById(repairOrder);
        }
    }

    private void activateContractAfterDeposit(RentalBill rentalBill, Integer operatorId) {
        RentalContract rentalContract = rentalContractMapper.selectById(rentalBill.getContractId());
        AssertUtils.notNull(rentalContract, "合同不存在");
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<RentalContract> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RentalContract::getId, rentalContract.getId())
                .eq(RentalContract::getStatus, RentalContractStatusEnum.STATUS_3.getType())
                .set(RentalContract::getStatus, RentalContractStatusEnum.STATUS_4.getType())
                .set(RentalContract::getUpdateTime, now);
        int updated = rentalContractMapper.update(null, updateWrapper);
        if (updated <= 0) {
            return;
        }

        RentalContractStatus rentalContractStatus = new RentalContractStatus();
        rentalContractStatus.setOriginStatus(RentalContractStatusEnum.STATUS_3.getType());
        rentalContractStatus.setNewId(RentalContractStatusEnum.STATUS_4.getType());
        rentalContractStatus.setRentalContractId(rentalContract.getId());
        rentalContractStatus.setOperatorId(operatorId);
        rentalContractStatus.setNote("押金支付成功，合同正式生效");
        rentalContractStatus.setCreateTime(now);
        rentalContractStatusMapper.insert(rentalContractStatus);

        rentalContract.setStatus(RentalContractStatusEnum.STATUS_4.getType());
        rentalContract.setUpdateTime(now);

        House house = houseMapper.selectById(rentalContract.getHouseId());
        if (house != null) {
            house.setStatus(HouseStatusEnum.STATUS_3.getType());
            houseMapper.updateById(house);
        }
        rentalBillService.createFirstRentBill(rentalContract);
    }

    private RentalBill getBillById(Integer billId) {
        RentalBill rentalBill = rentalBillMapper.selectById(billId);
        AssertUtils.notNull(rentalBill, "账单不存在");
        return rentalBill;
    }

    private RepairOrder getRepairOrderById(Integer repairOrderId) {
        RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
        AssertUtils.notNull(repairOrder, "报修工单不存在");
        return repairOrder;
    }

    private void assertBillAccess(RentalBillVO rentalBillVO) {
        boolean isAdmin = Objects.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole());
        boolean isTenant = Objects.equals(rentalBillVO.getTenantUserId(), LocalThreadHolder.getUserId());
        boolean isLandlord = Objects.equals(LocalThreadHolder.getUserId(), getLandlordUserId(rentalBillVO.getLandlordId()));
        AssertUtils.isTrue(isAdmin || isTenant || isLandlord, "非法操作");
    }

    private void assertRepairAccess(RepairOrderVO repairOrderVO) {
        boolean isAdmin = Objects.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole());
        boolean isTenant = Objects.equals(repairOrderVO.getTenantUserId(), LocalThreadHolder.getUserId());
        boolean isLandlord = Objects.equals(LocalThreadHolder.getUserId(), getLandlordUserId(repairOrderVO.getLandlordId()));
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
