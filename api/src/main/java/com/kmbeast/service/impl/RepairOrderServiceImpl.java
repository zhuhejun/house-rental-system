package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.mapper.RepairOrderMapper;
import com.kmbeast.mapper.RepairOrderStatusMapper;
import com.kmbeast.mapper.RentalContractMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RepairOrderQueryDto;
import com.kmbeast.pojo.em.RepairOrderStatusEnum;
import com.kmbeast.pojo.em.RepairPaymentStatusEnum;
import com.kmbeast.pojo.em.RepairTypeEnum;
import com.kmbeast.pojo.em.RentalContractStatusEnum;
import com.kmbeast.pojo.em.RoleEnum;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.entity.RepairOrder;
import com.kmbeast.pojo.entity.RepairOrderStatus;
import com.kmbeast.pojo.entity.RentalContract;
import com.kmbeast.pojo.vo.RepairOrderVO;
import com.kmbeast.service.RepairOrderService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 报修工单业务实现
 */
@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    @Resource
    private RentalContractMapper rentalContractMapper;
    @Resource
    private LandlordMapper landlordMapper;
    @Resource
    private RepairOrderStatusMapper repairOrderStatusMapper;

    @Override
    public Result<String> saveEntity(RepairOrder repairOrder) {
        validateCreate(repairOrder);
        RentalContract rentalContract = rentalContractMapper.selectById(repairOrder.getContractId());
        AssertUtils.notNull(rentalContract, "合同不存在");
        AssertUtils.equals(LocalThreadHolder.getUserId(), rentalContract.getTenantUserId(), "非法操作");
        AssertUtils.isTrue(canCreateRepair(rentalContract.getStatus()), "当前合同状态不支持发起报修");

        repairOrder.setRepairNo(generateRepairNo());
        repairOrder.setHouseId(rentalContract.getHouseId());
        repairOrder.setLandlordId(rentalContract.getLandlordId());
        repairOrder.setTenantUserId(rentalContract.getTenantUserId());
        repairOrder.setStatus(RepairOrderStatusEnum.STATUS_1.getType());
        repairOrder.setRepairAmount(BigDecimal.ZERO);
        repairOrder.setPaymentStatus(RepairPaymentStatusEnum.STATUS_3.getType());
        repairOrder.setCreateTime(LocalDateTime.now());
        repairOrder.setUpdateTime(LocalDateTime.now());
        save(repairOrder);
        return ApiResult.success("报修工单已提交，等待房东处理");
    }

    @Override
    public Result<List<RepairOrderVO>> list(RepairOrderQueryDto queryDto) {
        List<RepairOrderVO> data = this.baseMapper.list(queryDto);
        for (RepairOrderVO item : data) {
            item.setRepairTypeText(RepairTypeEnum.getDetail(item.getRepairType()));
        }
        Integer count = this.baseMapper.listCount(queryDto);
        return ApiResult.success(data, count);
    }

    @Override
    public Result<List<RepairOrderVO>> listUser(RepairOrderQueryDto queryDto) {
        queryDto.setTenantUserId(LocalThreadHolder.getUserId());
        return list(queryDto);
    }

    @Override
    public Result<List<RepairOrderVO>> listLandlord(RepairOrderQueryDto queryDto) {
        queryDto.setLandlordUserId(LocalThreadHolder.getUserId());
        return list(queryDto);
    }

    @Override
    public Result<RepairOrderVO> getById(Integer id) {
        AssertUtils.notNull(id, "报修工单ID不能为空");
        RepairOrderVO repairOrderVO = this.baseMapper.getById(id);
        AssertUtils.notNull(repairOrderVO, "报修工单不存在");
        repairOrderVO.setRepairTypeText(RepairTypeEnum.getDetail(repairOrderVO.getRepairType()));
        return ApiResult.success(repairOrderVO);
    }

    @Override
    public Result<String> landlordAccept(Integer id) {
        AssertUtils.notNull(id, "报修工单ID不能为空");
        RepairOrder repairOrder = getEntityById(id);
        AssertUtils.equals(LocalThreadHolder.getUserId(), getLandlordUserId(repairOrder.getLandlordId()), "非法操作");
        AssertUtils.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_1.getType(), "当前状态无法受理");
        repairOrder.setAcceptTime(LocalDateTime.now());
        repairOrder.setUpdateTime(LocalDateTime.now());
        updateStatus(repairOrder, RepairOrderStatusEnum.STATUS_2.getType(), "房东已受理报修工单");
        updateById(repairOrder);
        return ApiResult.success("工单已受理");
    }

    @Override
    public Result<String> landlordProcessing(Integer id) {
        AssertUtils.notNull(id, "报修工单ID不能为空");
        RepairOrder repairOrder = getEntityById(id);
        AssertUtils.equals(LocalThreadHolder.getUserId(), getLandlordUserId(repairOrder.getLandlordId()), "非法操作");
        AssertUtils.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_2.getType(), "当前状态无法进入处理中");
        repairOrder.setUpdateTime(LocalDateTime.now());
        updateStatus(repairOrder, RepairOrderStatusEnum.STATUS_3.getType(), "房东已开始处理报修工单");
        updateById(repairOrder);
        return ApiResult.success("工单已进入处理中");
    }

    @Override
    public Result<String> landlordFinish(RepairOrder repairOrder) {
        AssertUtils.notNull(repairOrder, "报修工单数据不能为空");
        AssertUtils.notNull(repairOrder.getId(), "报修工单ID不能为空");
        AssertUtils.hasText(repairOrder.getHandleNote(), "请填写处理结果");
        RepairOrder dbOrder = getEntityById(repairOrder.getId());
        AssertUtils.equals(LocalThreadHolder.getUserId(), getLandlordUserId(dbOrder.getLandlordId()), "非法操作");
        AssertUtils.isTrue(Objects.equals(dbOrder.getStatus(), RepairOrderStatusEnum.STATUS_2.getType())
                        || Objects.equals(dbOrder.getStatus(), RepairOrderStatusEnum.STATUS_3.getType()),
                "当前状态无法提交处理结果");
        BigDecimal repairAmount = repairOrder.getRepairAmount() == null ? BigDecimal.ZERO : repairOrder.getRepairAmount();
        AssertUtils.isTrue(repairAmount.compareTo(BigDecimal.ZERO) >= 0, "维修费用不能小于0");
        dbOrder.setHandleNote(repairOrder.getHandleNote());
        dbOrder.setHandleVoucherUrl(repairOrder.getHandleVoucherUrl());
        dbOrder.setRepairAmount(repairAmount);
        dbOrder.setPaymentStatus(repairAmount.compareTo(BigDecimal.ZERO) > 0
                ? RepairPaymentStatusEnum.STATUS_1.getType()
                : RepairPaymentStatusEnum.STATUS_3.getType());
        dbOrder.setFinishTime(LocalDateTime.now());
        dbOrder.setUpdateTime(LocalDateTime.now());
        String note = repairAmount.compareTo(BigDecimal.ZERO) > 0
                ? "房东已提交处理结果，等待租户支付维修费用后确认"
                : "房东已提交处理结果，等待租户确认";
        updateStatus(dbOrder, RepairOrderStatusEnum.STATUS_4.getType(), note);
        updateById(dbOrder);
        return ApiResult.success(repairAmount.compareTo(BigDecimal.ZERO) > 0
                ? "处理结果已提交，等待租户支付维修费用"
                : "处理结果已提交，等待租户确认");
    }

    @Override
    public Result<String> tenantConfirmFinish(Integer id) {
        AssertUtils.notNull(id, "报修工单ID不能为空");
        RepairOrder repairOrder = getEntityById(id);
        AssertUtils.equals(LocalThreadHolder.getUserId(), repairOrder.getTenantUserId(), "非法操作");
        AssertUtils.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_4.getType(), "当前状态无法确认完成");
        AssertUtils.notEquals(RepairPaymentStatusEnum.STATUS_1.getType(), repairOrder.getPaymentStatus(), "请先完成维修费用支付");
        repairOrder.setTenantConfirmTime(LocalDateTime.now());
        repairOrder.setUpdateTime(LocalDateTime.now());
        updateStatus(repairOrder, RepairOrderStatusEnum.STATUS_5.getType(), "租户已确认报修完成");
        updateById(repairOrder);
        return ApiResult.success("报修工单已完成");
    }

    @Override
    public Result<String> tenantCancel(RepairOrder repairOrder) {
        AssertUtils.notNull(repairOrder, "报修工单数据不能为空");
        AssertUtils.notNull(repairOrder.getId(), "报修工单ID不能为空");
        RepairOrder dbOrder = getEntityById(repairOrder.getId());
        AssertUtils.equals(LocalThreadHolder.getUserId(), dbOrder.getTenantUserId(), "非法操作");
        AssertUtils.isTrue(Objects.equals(dbOrder.getStatus(), RepairOrderStatusEnum.STATUS_1.getType())
                        || Objects.equals(dbOrder.getStatus(), RepairOrderStatusEnum.STATUS_2.getType()),
                "当前状态无法取消报修");
        dbOrder.setCancelReason(repairOrder.getCancelReason());
        dbOrder.setUpdateTime(LocalDateTime.now());
        updateStatus(dbOrder, RepairOrderStatusEnum.STATUS_6.getType(), repairOrder.getCancelReason());
        updateById(dbOrder);
        return ApiResult.success("报修工单已取消");
    }

    @Override
    public Result<String> adminReject(RepairOrder repairOrder) {
        AssertUtils.notNull(repairOrder, "报修工单数据不能为空");
        AssertUtils.notNull(repairOrder.getId(), "报修工单ID不能为空");
        AssertUtils.hasText(repairOrder.getRejectReason(), "请填写驳回原因");
        AssertUtils.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole(), "非法操作");
        RepairOrder dbOrder = getEntityById(repairOrder.getId());
        AssertUtils.isTrue(Objects.equals(dbOrder.getStatus(), RepairOrderStatusEnum.STATUS_1.getType())
                        || Objects.equals(dbOrder.getStatus(), RepairOrderStatusEnum.STATUS_2.getType())
                        || Objects.equals(dbOrder.getStatus(), RepairOrderStatusEnum.STATUS_3.getType())
                        || Objects.equals(dbOrder.getStatus(), RepairOrderStatusEnum.STATUS_4.getType()),
                "当前状态无法驳回");
        dbOrder.setRejectReason(repairOrder.getRejectReason());
        dbOrder.setUpdateTime(LocalDateTime.now());
        updateStatus(dbOrder, RepairOrderStatusEnum.STATUS_7.getType(), repairOrder.getRejectReason());
        updateById(dbOrder);
        return ApiResult.success("报修工单已驳回");
    }

    private void validateCreate(RepairOrder repairOrder) {
        AssertUtils.notNull(repairOrder, "报修工单数据不能为空");
        AssertUtils.notNull(repairOrder.getContractId(), "关联合同不能为空");
        AssertUtils.hasText(repairOrder.getTitle(), "报修标题不能为空");
        AssertUtils.notNull(repairOrder.getRepairType(), "报修类型不能为空");
        AssertUtils.hasText(repairOrder.getContent(), "问题描述不能为空");
        AssertUtils.isTrue(RepairTypeEnum.contains(repairOrder.getRepairType()), "报修类型非法");
        if (repairOrder.getIsUrgent() == null) {
            repairOrder.setIsUrgent(0);
        }
        AssertUtils.isTrue(Objects.equals(repairOrder.getIsUrgent(), 0) || Objects.equals(repairOrder.getIsUrgent(), 1),
                "紧急程度非法");
    }

    private boolean canCreateRepair(Integer contractStatus) {
        return Objects.equals(contractStatus, RentalContractStatusEnum.STATUS_4.getType())
                || Objects.equals(contractStatus, RentalContractStatusEnum.STATUS_14.getType())
                || Objects.equals(contractStatus, RentalContractStatusEnum.STATUS_9.getType())
                || Objects.equals(contractStatus, RentalContractStatusEnum.STATUS_10.getType())
                || Objects.equals(contractStatus, RentalContractStatusEnum.STATUS_11.getType())
                || Objects.equals(contractStatus, RentalContractStatusEnum.STATUS_12.getType());
    }

    private RepairOrder getEntityById(Integer id) {
        RepairOrder repairOrder = this.baseMapper.selectById(id);
        AssertUtils.notNull(repairOrder, "报修工单不存在");
        return repairOrder;
    }

    private Integer getLandlordUserId(Integer landlordId) {
        if (landlordId == null) {
            return null;
        }
        Landlord landlord = landlordMapper.selectById(landlordId);
        if (landlord == null) {
            return null;
        }
        return landlord.getUserId();
    }

    private void updateStatus(RepairOrder repairOrder, Integer targetStatus, String note) {
        Integer originStatus = repairOrder.getStatus();
        if (!Objects.equals(originStatus, targetStatus)) {
            RepairOrderStatus repairOrderStatus = new RepairOrderStatus();
            repairOrderStatus.setOriginStatus(originStatus);
            repairOrderStatus.setNewId(targetStatus);
            repairOrderStatus.setRepairOrderId(repairOrder.getId());
            repairOrderStatus.setOperatorId(LocalThreadHolder.getUserId());
            repairOrderStatus.setNote(note);
            repairOrderStatus.setCreateTime(LocalDateTime.now());
            repairOrderStatusMapper.insert(repairOrderStatus);
        }
        repairOrder.setStatus(targetStatus);
    }

    private String generateRepairNo() {
        int random = new Random().nextInt(9000) + 1000;
        return "RP" + System.currentTimeMillis() + random;
    }
}
