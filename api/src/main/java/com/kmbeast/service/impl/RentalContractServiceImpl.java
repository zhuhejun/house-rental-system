package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.mapper.HouseOrderInfoMapper;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.mapper.RentalContractMapper;
import com.kmbeast.mapper.RentalContractStatusMapper;
import com.kmbeast.mapper.RentalTerminationMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.dto.RentalContractQueryDto;
import com.kmbeast.pojo.em.HouseDepositEnum;
import com.kmbeast.pojo.em.HouseOrderStatusEnum;
import com.kmbeast.pojo.em.HouseStatusEnum;
import com.kmbeast.pojo.em.RentalContractStatusEnum;
import com.kmbeast.pojo.em.RoleEnum;
import com.kmbeast.pojo.em.UtilityPaymentModeEnum;
import com.kmbeast.pojo.entity.House;
import com.kmbeast.pojo.entity.HouseOrderInfo;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.entity.RentalContract;
import com.kmbeast.pojo.entity.RentalContractStatus;
import com.kmbeast.pojo.entity.RentalTermination;
import com.kmbeast.pojo.vo.LandlordVO;
import com.kmbeast.pojo.vo.RentalContractVO;
import com.kmbeast.service.RentalBillService;
import com.kmbeast.service.RentalContractService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 租赁合同业务实现
 */
@Service
public class RentalContractServiceImpl extends ServiceImpl<RentalContractMapper, RentalContract>
        implements RentalContractService {

    @Resource
    private HouseOrderInfoMapper houseOrderInfoMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private LandlordMapper landlordMapper;
    @Resource
    private RentalContractStatusMapper rentalContractStatusMapper;
    @Resource
    private RentalTerminationMapper rentalTerminationMapper;
    @Resource
    private RentalBillService rentalBillService;

    private static final int TERMINATION_STATUS_REJECTED = 15;

    @Override
    public Result<String> saveEntity(RentalContract rentalContract) {
        validateCreate(rentalContract);
        HouseOrderInfo orderInfo = houseOrderInfoMapper.selectById(rentalContract.getHouseOrderInfoId());
        House house = houseMapper.selectById(orderInfo.getHouseId());
        AssertUtils.notNull(house, "房源信息异常");
        LandlordVO landlord = getLandlord();
        AssertUtils.equals(house.getLandlordId(), landlord.getId(), "非法操作");

        rentalContract.setHouseId(orderInfo.getHouseId());
        rentalContract.setTenantUserId(orderInfo.getUserId());
        rentalContract.setLandlordId(house.getLandlordId());
        rentalContract.setContractNo(generateContractNo());
        applyDepositMethod(rentalContract);
        rentalContract.setStatus(RentalContractStatusEnum.STATUS_1.getType());
        rentalContract.setCreateTime(LocalDateTime.now());
        rentalContract.setUpdateTime(LocalDateTime.now());
        save(rentalContract);
        return ApiResult.success("租赁合同发起成功，等待管理员审核");
    }

    @Override
    public Result<List<RentalContractVO>> list(RentalContractQueryDto queryDto) {
        List<RentalContractVO> data = this.baseMapper.list(queryDto);
        Integer count = this.baseMapper.listCount(queryDto);
        return ApiResult.success(data, count);
    }

    @Override
    public Result<List<RentalContractVO>> listUser(RentalContractQueryDto queryDto) {
        queryDto.setTenantUserId(LocalThreadHolder.getUserId());
        queryDto.setTenantVisible(true);
        return list(queryDto);
    }

    @Override
    public Result<List<RentalContractVO>> listLandlord(RentalContractQueryDto queryDto) {
        queryDto.setLandlordUserId(LocalThreadHolder.getUserId());
        return list(queryDto);
    }

    @Override
    public Result<RentalContractVO> getById(Integer id) {
        AssertUtils.notNull(id, "合同ID不能为空");
        RentalContractVO rentalContractVO = this.baseMapper.getById(id);
        AssertUtils.notNull(rentalContractVO, "合同不存在");
        return ApiResult.success(rentalContractVO);
    }

    @Override
    public Result<String> adminApprove(Integer id) {
        AssertUtils.notNull(id, "合同ID不能为空");
        AssertUtils.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole(), "非法操作");
        RentalContract rentalContract = getEntityById(id);
        AssertUtils.equals(rentalContract.getStatus(), RentalContractStatusEnum.STATUS_1.getType(), "当前状态无法审核通过");
        updateStatus(rentalContract, RentalContractStatusEnum.STATUS_2.getType(), "管理员审核通过");
        rentalContract.setUpdateTime(LocalDateTime.now());
        updateById(rentalContract);
        return ApiResult.success("合同审核通过，已发放给租客确认");
    }

    @Override
    public Result<String> adminReject(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        AssertUtils.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole(), "非法操作");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_1.getType(), "当前状态无法驳回");
        dbContract.setRejectReason(rentalContract.getRejectReason());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_5.getType(), rentalContract.getRejectReason());
        dbContract.setUpdateTime(LocalDateTime.now());
        updateById(dbContract);
        return ApiResult.success("合同已驳回");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> tenantConfirm(Integer id) {
        AssertUtils.notNull(id, "合同ID不能为空");
        RentalContract rentalContract = getEntityById(id);
        AssertUtils.equals(rentalContract.getTenantUserId(), LocalThreadHolder.getUserId(), "非法操作");
        AssertUtils.equals(rentalContract.getStatus(), RentalContractStatusEnum.STATUS_2.getType(), "当前状态无法确认");
        Integer activeCount = this.baseMapper.countActiveContractByHouseId(rentalContract.getHouseId(), rentalContract.getId());
        AssertUtils.isFalse(activeCount != null && activeCount > 0, "该房源已有其他进行中的合同");
        updateStatus(rentalContract, RentalContractStatusEnum.STATUS_3.getType(), null);
        rentalContract.setConfirmTime(LocalDateTime.now());
        rentalContract.setUpdateTime(LocalDateTime.now());
        updateById(rentalContract);
        rentalBillService.createDepositBill(rentalContract);
        return ApiResult.success("合同已确认，请先完成押金支付");
    }

    @Override
    public Result<String> tenantReject(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        AssertUtils.equals(dbContract.getTenantUserId(), LocalThreadHolder.getUserId(), "非法操作");
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_2.getType(), "当前状态无法拒绝");
        dbContract.setRejectReason(rentalContract.getRejectReason());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_6.getType(), rentalContract.getRejectReason());
        dbContract.setUpdateTime(LocalDateTime.now());
        updateById(dbContract);
        return ApiResult.success("合同已拒绝");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> cancel(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        boolean isAdmin = Objects.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole());
        boolean isLandlord = Objects.equals(LocalThreadHolder.getUserId(), getLandlordUserId(dbContract.getLandlordId()));
        boolean isTenant = Objects.equals(LocalThreadHolder.getUserId(), dbContract.getTenantUserId());
        AssertUtils.isTrue(isAdmin || isLandlord || isTenant, "非法操作");
        AssertUtils.isFalse(Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_4.getType()),
                "已生效合同请通过退租流程处理");
        AssertUtils.isFalse(Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_5.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_6.getType()),
                "当前状态无法取消");
        AssertUtils.isFalse(Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_7.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_8.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_9.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_10.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_11.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_12.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_13.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_14.getType()),
                "当前状态无法取消");

        dbContract.setCancelReason(rentalContract.getCancelReason());
        Integer beforeStatus = dbContract.getStatus();
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_7.getType(), rentalContract.getCancelReason());
        dbContract.setUpdateTime(LocalDateTime.now());
        updateContractNullable(dbContract);
        rentalBillService.cancelPendingBillsByContractId(dbContract.getId());
        if (Objects.equals(beforeStatus, RentalContractStatusEnum.STATUS_4.getType())) {
            recoverHouseStatusIfNeeded(dbContract.getHouseId());
        }
        return ApiResult.success("合同已取消");
    }

    @Override
    public Result<String> applyTerminate(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        AssertUtils.hasText(rentalContract.getTerminationReason(), "请填写退租原因");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        boolean isLandlord = Objects.equals(LocalThreadHolder.getUserId(), getLandlordUserId(dbContract.getLandlordId()));
        boolean isTenant = Objects.equals(LocalThreadHolder.getUserId(), dbContract.getTenantUserId());
        AssertUtils.isTrue(isLandlord || isTenant, "非法操作");
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_4.getType(), "仅已生效合同可申请退租");
        AssertUtils.isFalse(getActiveTermination(dbContract.getId()) != null, "当前合同已有进行中的退租申请");
        RentalTermination rentalTermination = new RentalTermination();
        rentalTermination.setRentalContractId(dbContract.getId());
        rentalTermination.setHouseId(dbContract.getHouseId());
        rentalTermination.setLandlordId(dbContract.getLandlordId());
        rentalTermination.setTenantUserId(dbContract.getTenantUserId());
        rentalTermination.setApplyUserId(LocalThreadHolder.getUserId());
        rentalTermination.setApplyReason(rentalContract.getTerminationReason());
        rentalTermination.setStatus(RentalContractStatusEnum.STATUS_14.getType());
        rentalTermination.setApplyTime(LocalDateTime.now());
        rentalTermination.setCreateTime(LocalDateTime.now());
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.insert(rentalTermination);
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_14.getType(), rentalContract.getTerminationReason());
        dbContract.setUpdateTime(LocalDateTime.now());
        updateById(dbContract);
        return ApiResult.success(isLandlord ? "退租申请已提交，等待租客确认" : "退租申请已提交，等待房东确认");
    }

    @Override
    public Result<String> confirmTerminate(Integer id) {
        AssertUtils.notNull(id, "合同ID不能为空");
        RentalContract dbContract = getEntityById(id);
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_14.getType(), "当前状态无法确认退租");
        RentalTermination rentalTermination = getActiveTermination(dbContract.getId());
        AssertUtils.notNull(rentalTermination, "退租申请不存在");
        assertCounterparty(dbContract, rentalTermination);
        rentalTermination.setCounterpartyUserId(LocalThreadHolder.getUserId());
        rentalTermination.setCounterpartyRejectReason(null);
        rentalTermination.setCounterpartyDecisionTime(LocalDateTime.now());
        rentalTermination.setStatus(RentalContractStatusEnum.STATUS_9.getType());
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.updateById(rentalTermination);
        dbContract.setUpdateTime(LocalDateTime.now());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_9.getType(), "退租申请已由对方确认，等待房东提交退租结算");
        updateById(dbContract);
        return ApiResult.success("你已同意退租，下一步由房东提交退租结算");
    }

    @Override
    public Result<String> rejectTerminate(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        AssertUtils.hasText(rentalContract.getTerminationCounterpartyRejectReason(), "请填写拒绝原因");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_14.getType(), "当前状态无法拒绝退租");
        RentalTermination rentalTermination = getActiveTermination(dbContract.getId());
        AssertUtils.notNull(rentalTermination, "退租申请不存在");
        assertCounterparty(dbContract, rentalTermination);
        rentalTermination.setCounterpartyUserId(LocalThreadHolder.getUserId());
        rentalTermination.setCounterpartyRejectReason(rentalContract.getTerminationCounterpartyRejectReason());
        rentalTermination.setCounterpartyDecisionTime(LocalDateTime.now());
        rentalTermination.setStatus(TERMINATION_STATUS_REJECTED);
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.updateById(rentalTermination);
        dbContract.setUpdateTime(LocalDateTime.now());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_4.getType(), "退租申请被对方拒绝：" + rentalContract.getTerminationCounterpartyRejectReason());
        updateById(dbContract);
        return ApiResult.success("你已拒绝退租申请，合同恢复为已生效");
    }

    @Override
    public Result<String> submitTerminateSettlement(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        AssertUtils.notNull(rentalContract.getTerminationRefundAmount(), "请填写退还押金金额");
        AssertUtils.hasText(rentalContract.getTerminationVoucherUrl(), "请上传退押凭证");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        AssertUtils.equals(LocalThreadHolder.getUserId(), getLandlordUserId(dbContract.getLandlordId()), "非法操作");
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_9.getType(), "当前状态无法提交退租结算");
        RentalTermination rentalTermination = getActiveTermination(dbContract.getId());
        AssertUtils.notNull(rentalTermination, "退租申请不存在");
        AssertUtils.notNegative(rentalContract.getTerminationRefundAmount(), "退还押金金额不能小于0");
        AssertUtils.isTrue(rentalContract.getTerminationRefundAmount().compareTo(defaultAmount(dbContract.getDepositAmount())) <= 0,
                "退还押金金额不能大于合同押金");
        rentalTermination.setRefundAmount(rentalContract.getTerminationRefundAmount());
        rentalTermination.setSettlementVoucherUrl(rentalContract.getTerminationVoucherUrl());
        rentalTermination.setSettlementVoucherNote(rentalContract.getTerminationVoucherNote());
        rentalTermination.setAdminAuditNote(null);
        rentalTermination.setAdminAuditTime(null);
        rentalTermination.setStatus(RentalContractStatusEnum.STATUS_10.getType());
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.updateById(rentalTermination);
        dbContract.setUpdateTime(LocalDateTime.now());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_10.getType(), "房东已提交退租结算，进入待退租审核");
        updateById(dbContract);
        return ApiResult.success("退租结算已提交，当前状态为待退租审核");
    }

    @Override
    public Result<String> adminApproveTerminate(Integer id) {
        AssertUtils.notNull(id, "合同ID不能为空");
        AssertUtils.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole(), "非法操作");
        RentalContract dbContract = getEntityById(id);
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_10.getType(), "当前状态无法审核通过");
        RentalTermination rentalTermination = getActiveTermination(dbContract.getId());
        AssertUtils.notNull(rentalTermination, "退租申请不存在");
        rentalTermination.setAdminAuditNote(null);
        rentalTermination.setAdminAuditTime(LocalDateTime.now());
        rentalTermination.setStatus(RentalContractStatusEnum.STATUS_11.getType());
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.updateById(rentalTermination);
        dbContract.setUpdateTime(LocalDateTime.now());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_11.getType(), "退租审核通过，进入待退押");
        updateById(dbContract);
        return ApiResult.success("退租已审核通过，当前状态为待退押");
    }

    @Override
    public Result<String> adminRejectTerminate(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        AssertUtils.hasText(rentalContract.getTerminationAuditNote(), "请填写驳回原因");
        AssertUtils.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole(), "非法操作");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_10.getType(), "当前状态无法驳回");
        RentalTermination rentalTermination = getActiveTermination(dbContract.getId());
        AssertUtils.notNull(rentalTermination, "退租申请不存在");
        rentalTermination.setAdminAuditNote(rentalContract.getTerminationAuditNote());
        rentalTermination.setAdminAuditTime(LocalDateTime.now());
        rentalTermination.setStatus(RentalContractStatusEnum.STATUS_9.getType());
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.updateById(rentalTermination);
        dbContract.setUpdateTime(LocalDateTime.now());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_9.getType(), "退租审核驳回：" + rentalContract.getTerminationAuditNote());
        updateById(dbContract);
        return ApiResult.success("退租申请已驳回，房东可根据意见重新提交");
    }

    @Override
    public Result<String> submitTerminateRefund(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        AssertUtils.hasText(rentalContract.getTerminationRefundVoucherUrl(), "请上传退押凭证");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        AssertUtils.equals(LocalThreadHolder.getUserId(), getLandlordUserId(dbContract.getLandlordId()), "非法操作");
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_11.getType(), "当前状态无法提交退押凭证");
        RentalTermination rentalTermination = getActiveTermination(dbContract.getId());
        AssertUtils.notNull(rentalTermination, "退租申请不存在");
        rentalTermination.setRefundVoucherUrl(rentalContract.getTerminationRefundVoucherUrl());
        rentalTermination.setRefundVoucherNote(rentalContract.getTerminationRefundVoucherNote());
        rentalTermination.setRefundTime(LocalDateTime.now());
        rentalTermination.setAdminAuditNote(null);
        rentalTermination.setAdminAuditTime(null);
        rentalTermination.setStatus(RentalContractStatusEnum.STATUS_12.getType());
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.updateById(rentalTermination);
        dbContract.setUpdateTime(LocalDateTime.now());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_12.getType(), "房东已提交退押凭证，进入待审核退押");
        updateById(dbContract);
        return ApiResult.success("退押凭证已提交，当前状态为待审核退押");
    }

    @Override
    public Result<String> adminApproveTerminateRefund(Integer id) {
        AssertUtils.notNull(id, "合同ID不能为空");
        AssertUtils.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole(), "非法操作");
        RentalContract dbContract = getEntityById(id);
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_12.getType(), "当前状态无法审核通过");
        RentalTermination rentalTermination = getActiveTermination(dbContract.getId());
        AssertUtils.notNull(rentalTermination, "退租申请不存在");
        rentalTermination.setAdminAuditNote(null);
        rentalTermination.setAdminAuditTime(LocalDateTime.now());
        rentalTermination.setStatus(RentalContractStatusEnum.STATUS_13.getType());
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.updateById(rentalTermination);
        dbContract.setUpdateTime(LocalDateTime.now());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_13.getType(), "管理员审核通过退押凭证，合同已退租");
        updateById(dbContract);
        rentalBillService.cancelPendingBillsByContractId(dbContract.getId());
        recoverHouseStatusIfNeeded(dbContract.getHouseId());
        return ApiResult.success("退押凭证审核通过，合同已退租");
    }

    @Override
    public Result<String> adminRejectTerminateRefund(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        AssertUtils.hasText(rentalContract.getTerminationAuditNote(), "请填写驳回原因");
        AssertUtils.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole(), "非法操作");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_12.getType(), "当前状态无法驳回");
        RentalTermination rentalTermination = getActiveTermination(dbContract.getId());
        AssertUtils.notNull(rentalTermination, "退租申请不存在");
        rentalTermination.setAdminAuditNote(rentalContract.getTerminationAuditNote());
        rentalTermination.setAdminAuditTime(LocalDateTime.now());
        rentalTermination.setStatus(RentalContractStatusEnum.STATUS_11.getType());
        rentalTermination.setUpdateTime(LocalDateTime.now());
        rentalTerminationMapper.updateById(rentalTermination);
        dbContract.setUpdateTime(LocalDateTime.now());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_11.getType(), "退押凭证审核驳回：" + rentalContract.getTerminationAuditNote());
        updateById(dbContract);
        return ApiResult.success("退押凭证已驳回，房东可根据意见重新提交");
    }

    private RentalContract getEntityById(Integer id) {
        RentalContract rentalContract = this.baseMapper.selectById(id);
        AssertUtils.notNull(rentalContract, "合同不存在");
        return rentalContract;
    }

    private void validateCreate(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getHouseOrderInfoId(), "预约记录不能为空");
        AssertUtils.hasText(rentalContract.getTitle(), "合同标题不能为空");
        AssertUtils.hasText(rentalContract.getStartDate(), "起租日期不能为空");
        AssertUtils.hasText(rentalContract.getEndDate(), "到期日期不能为空");
        AssertUtils.notNull(rentalContract.getMonthlyRent(), "月租金不能为空");
        AssertUtils.notNull(rentalContract.getDepositMethodId(), "押金方式不能为空");
        if (rentalContract.getUtilityPaymentMode() == null) {
            rentalContract.setUtilityPaymentMode(UtilityPaymentModeEnum.STATUS_2.getType());
        }
        AssertUtils.isTrue(Objects.equals(rentalContract.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_1.getType())
                        || Objects.equals(rentalContract.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_2.getType()),
                "水电费支付方式非法");
        AssertUtils.isTrue(rentalContract.getMonthlyRent().compareTo(BigDecimal.ZERO) > 0, "月租金必须大于0");
        AssertUtils.isTrue(HouseDepositEnum.getDepositMonths(rentalContract.getDepositMethodId()) > 0, "押金方式非法");

        HouseOrderInfo orderInfo = houseOrderInfoMapper.selectById(rentalContract.getHouseOrderInfoId());
        AssertUtils.notNull(orderInfo, "预约记录不存在");
        AssertUtils.equals(orderInfo.getOrderStatus(), HouseOrderStatusEnum.STATUS_5.getType(), "仅已完成的预约可发起合同");

        Integer contractCount = this.baseMapper.countContractByOrderId(rentalContract.getHouseOrderInfoId());
        AssertUtils.isFalse(contractCount != null && contractCount > 0, "该预约已创建过合同");

        Integer activeCount = this.baseMapper.countActiveContractByHouseId(orderInfo.getHouseId(), null);
        AssertUtils.isFalse(activeCount != null && activeCount > 0, "该房源已有待审核、待确认、待支付押金或生效中的合同");
    }

    private LandlordVO getLandlord() {
        LandlordQueryDto landlordQueryDto = new LandlordQueryDto();
        landlordQueryDto.setUserId(LocalThreadHolder.getUserId());
        List<LandlordVO> landlordVOS = landlordMapper.list(landlordQueryDto);
        AssertUtils.isFalse(landlordVOS.isEmpty(), "房东信息异常，非法操作");
        LandlordVO landlordVO = landlordVOS.get(0);
        AssertUtils.isTrue(landlordVO.getIsAudit(), "房东信息未审核");
        return landlordVO;
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

    private void assertCounterparty(RentalContract rentalContract, RentalTermination rentalTermination) {
        Integer landlordUserId = getLandlordUserId(rentalContract.getLandlordId());
        Integer applicantUserId = rentalTermination.getApplyUserId();
        AssertUtils.notNull(applicantUserId, "退租申请人信息异常");
        boolean isLandlord = Objects.equals(LocalThreadHolder.getUserId(), landlordUserId);
        boolean isTenant = Objects.equals(LocalThreadHolder.getUserId(), rentalContract.getTenantUserId());
        AssertUtils.isTrue(isLandlord || isTenant, "非法操作");
        AssertUtils.isFalse(Objects.equals(LocalThreadHolder.getUserId(), applicantUserId), "发起方不能确认或拒绝自己的退租申请");
    }

    private RentalTermination getActiveTermination(Integer rentalContractId) {
        return rentalTerminationMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RentalTermination>()
                        .eq(RentalTermination::getRentalContractId, rentalContractId)
                        .in(RentalTermination::getStatus,
                                RentalContractStatusEnum.STATUS_14.getType(),
                                RentalContractStatusEnum.STATUS_9.getType(),
                                RentalContractStatusEnum.STATUS_10.getType(),
                                RentalContractStatusEnum.STATUS_11.getType(),
                                RentalContractStatusEnum.STATUS_12.getType())
                        .orderByDesc(RentalTermination::getId)
                        .last("limit 1")
        );
    }

    private void updateStatus(RentalContract rentalContract, Integer targetStatus, String note) {
        Integer originStatus = rentalContract.getStatus();
        if (!Objects.equals(originStatus, targetStatus)) {
            RentalContractStatus rentalContractStatus = new RentalContractStatus();
            rentalContractStatus.setOriginStatus(originStatus);
            rentalContractStatus.setNewId(targetStatus);
            rentalContractStatus.setRentalContractId(rentalContract.getId());
            rentalContractStatus.setOperatorId(LocalThreadHolder.getUserId());
            rentalContractStatus.setNote(note);
            rentalContractStatus.setCreateTime(LocalDateTime.now());
            rentalContractStatusMapper.insert(rentalContractStatus);
        }
        rentalContract.setStatus(targetStatus);
    }

    private void recoverHouseStatusIfNeeded(Integer houseId) {
        Integer activeCount = this.baseMapper.countActiveContractByHouseId(houseId, null);
        if (activeCount != null && activeCount > 0) {
            return;
        }
        House house = houseMapper.selectById(houseId);
        if (house != null && Objects.equals(house.getStatus(), HouseStatusEnum.STATUS_3.getType())) {
            house.setStatus(HouseStatusEnum.STATUS_1.getType());
            houseMapper.updateById(house);
        }
    }

    private void updateContractNullable(RentalContract rentalContract) {
        this.lambdaUpdate()
                .eq(RentalContract::getId, rentalContract.getId())
                .set(RentalContract::getStatus, rentalContract.getStatus())
                .set(RentalContract::getRejectReason, rentalContract.getRejectReason())
                .set(RentalContract::getCancelReason, rentalContract.getCancelReason())
                .set(RentalContract::getTerminationReason, rentalContract.getTerminationReason())
                .set(RentalContract::getTerminationApplyUserId, rentalContract.getTerminationApplyUserId())
                .set(RentalContract::getTerminationRefundAmount, rentalContract.getTerminationRefundAmount())
                .set(RentalContract::getTerminationVoucherUrl, rentalContract.getTerminationVoucherUrl())
                .set(RentalContract::getTerminationVoucherNote, rentalContract.getTerminationVoucherNote())
                .set(RentalContract::getTerminationCounterpartyRejectReason, rentalContract.getTerminationCounterpartyRejectReason())
                .set(RentalContract::getTerminationAuditNote, rentalContract.getTerminationAuditNote())
                .set(RentalContract::getTerminationRefundVoucherUrl, rentalContract.getTerminationRefundVoucherUrl())
                .set(RentalContract::getTerminationRefundVoucherNote, rentalContract.getTerminationRefundVoucherNote())
                .set(RentalContract::getConfirmTime, rentalContract.getConfirmTime())
                .set(RentalContract::getTerminationApplyTime, rentalContract.getTerminationApplyTime())
                .set(RentalContract::getTerminationAuditTime, rentalContract.getTerminationAuditTime())
                .set(RentalContract::getTerminationRefundTime, rentalContract.getTerminationRefundTime())
                .set(RentalContract::getUpdateTime, rentalContract.getUpdateTime())
                .update();
    }

    private String generateContractNo() {
        int random = new Random().nextInt(9000) + 1000;
        return "RC" + System.currentTimeMillis() + random;
    }

    private void applyDepositMethod(RentalContract rentalContract) {
        Integer depositMonths = HouseDepositEnum.getDepositMonths(rentalContract.getDepositMethodId());
        AssertUtils.isTrue(depositMonths > 0, "押金方式非法");
        rentalContract.setPayCycle(HouseDepositEnum.getDetail(rentalContract.getDepositMethodId()));
        rentalContract.setDepositAmount(rentalContract.getMonthlyRent().multiply(BigDecimal.valueOf(depositMonths)));
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
