package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.mapper.RentalBillMapper;
import com.kmbeast.mapper.RentalContractMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.dto.RentalBillQueryDto;
import com.kmbeast.pojo.em.RentalBillPayStatusEnum;
import com.kmbeast.pojo.em.RentalBillTypeEnum;
import com.kmbeast.pojo.em.RoleEnum;
import com.kmbeast.pojo.em.RentalContractStatusEnum;
import com.kmbeast.pojo.em.UtilityPaymentModeEnum;
import com.kmbeast.pojo.em.UtilityProofStatusEnum;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.entity.RentalBill;
import com.kmbeast.pojo.entity.RentalContract;
import com.kmbeast.pojo.vo.LandlordVO;
import com.kmbeast.pojo.vo.RentalBillVO;
import com.kmbeast.service.RentalBillService;
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
 * 租赁账单业务实现
 */
@Service
public class RentalBillServiceImpl extends ServiceImpl<RentalBillMapper, RentalBill> implements RentalBillService {

    @Resource
    private RentalContractMapper rentalContractMapper;
    @Resource
    private LandlordMapper landlordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> saveEntity(RentalBill rentalBill) {
        validateCreate(rentalBill);
        RentalContract rentalContract = rentalContractMapper.selectById(rentalBill.getContractId());
        LandlordVO landlord = getLandlord();
        AssertUtils.equals(rentalContract.getLandlordId(), landlord.getId(), "非法操作");

        rentalBill.setBillNo(generateBillNo());
        rentalBill.setHouseId(rentalContract.getHouseId());
        rentalBill.setLandlordId(rentalContract.getLandlordId());
        rentalBill.setTenantUserId(rentalContract.getTenantUserId());
        rentalBill.setBillType(RentalBillTypeEnum.STATUS_2.getType());
        rentalBill.setRentAmount(defaultAmount(rentalBill.getRentAmount(), rentalContract.getMonthlyRent()));
        rentalBill.setDepositAmount(BigDecimal.ZERO);
        rentalBill.setWaterAmount(defaultAmount(rentalBill.getWaterAmount(), BigDecimal.ZERO));
        rentalBill.setElectricAmount(defaultAmount(rentalBill.getElectricAmount(), BigDecimal.ZERO));
        if (rentalBill.getUtilityPaymentMode() == null) {
            rentalBill.setUtilityPaymentMode(rentalContract.getUtilityPaymentMode());
        }
        rentalBill.setTotalAmount(calculateTotalAmount(rentalBill));
        rentalBill.setUtilityProofStatus(resolveProofStatus(rentalBill.getUtilityPaymentMode()));
        rentalBill.setPayStatus(RentalBillPayStatusEnum.STATUS_1.getType());
        rentalBill.setCreateTime(LocalDateTime.now());
        rentalBill.setUpdateTime(LocalDateTime.now());
        save(rentalBill);
        return ApiResult.success("账单创建成功");
    }

    @Override
    public Result<List<RentalBillVO>> listUser(RentalBillQueryDto queryDto) {
        queryDto.setTenantUserId(LocalThreadHolder.getUserId());
        return list(queryDto);
    }

    @Override
    public Result<List<RentalBillVO>> listLandlord(RentalBillQueryDto queryDto) {
        LandlordVO landlord = getLandlord();
        queryDto.setLandlordId(landlord.getId());
        return list(queryDto);
    }

    @Override
    public Result<List<RentalBillVO>> list(RentalBillQueryDto queryDto) {
        List<RentalBillVO> data = this.baseMapper.list(queryDto);
        Integer count = this.baseMapper.listCount(queryDto);
        return ApiResult.success(data, count);
    }

    @Override
    public Result<RentalBillVO> getById(Integer id) {
        AssertUtils.notNull(id, "账单ID不能为空");
        RentalBillVO rentalBillVO = this.baseMapper.getById(id);
        AssertUtils.notNull(rentalBillVO, "账单不存在");
        assertBillAccess(rentalBillVO);
        return ApiResult.success(rentalBillVO);
    }

    @Override
    public Result<String> submitUtilityProof(RentalBill rentalBill) {
        AssertUtils.notNull(rentalBill, "账单数据不能为空");
        AssertUtils.notNull(rentalBill.getId(), "账单ID不能为空");
        AssertUtils.hasText(rentalBill.getUtilityProofUrl(), "请先上传缴费凭证");
        RentalBill dbBill = getByIdInternal(rentalBill.getId());
        AssertUtils.equals(dbBill.getTenantUserId(), LocalThreadHolder.getUserId(), "非法操作");
        AssertUtils.equals(dbBill.getBillType(), RentalBillTypeEnum.STATUS_2.getType(), "当前账单无需上传凭证");
        AssertUtils.equals(dbBill.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_1.getType(), "当前账单无需上传凭证");
        AssertUtils.isFalse(Objects.equals(dbBill.getUtilityProofStatus(), UtilityProofStatusEnum.STATUS_3.getType()),
                "凭证已审核通过，无需重复提交");
        dbBill.setUtilityProofUrl(rentalBill.getUtilityProofUrl());
        dbBill.setUtilityProofNote(rentalBill.getUtilityProofNote());
        dbBill.setUtilityProofStatus(UtilityProofStatusEnum.STATUS_2.getType());
        dbBill.setUpdateTime(LocalDateTime.now());
        updateById(dbBill);
        return ApiResult.success("水电费凭证已提交，等待房东审核");
    }

    @Override
    public Result<String> auditUtilityProof(RentalBill rentalBill) {
        AssertUtils.notNull(rentalBill, "账单数据不能为空");
        AssertUtils.notNull(rentalBill.getId(), "账单ID不能为空");
        AssertUtils.notNull(rentalBill.getUtilityProofStatus(), "审核结果不能为空");
        RentalBill dbBill = getByIdInternal(rentalBill.getId());
        RentalContract rentalContract = rentalContractMapper.selectById(dbBill.getContractId());
        AssertUtils.notNull(rentalContract, "合同不存在");
        LandlordVO landlord = getLandlord();
        AssertUtils.equals(rentalContract.getLandlordId(), landlord.getId(), "非法操作");
        AssertUtils.equals(dbBill.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_1.getType(), "当前账单无需审核凭证");
        AssertUtils.equals(dbBill.getUtilityProofStatus(), UtilityProofStatusEnum.STATUS_2.getType(), "当前状态无法审核");
        AssertUtils.isTrue(Objects.equals(rentalBill.getUtilityProofStatus(), UtilityProofStatusEnum.STATUS_3.getType())
                        || Objects.equals(rentalBill.getUtilityProofStatus(), UtilityProofStatusEnum.STATUS_4.getType()),
                "审核状态非法");
        dbBill.setUtilityProofStatus(rentalBill.getUtilityProofStatus());
        dbBill.setUtilityProofNote(rentalBill.getUtilityProofNote());
        dbBill.setUpdateTime(LocalDateTime.now());
        updateById(dbBill);
        return ApiResult.success(Objects.equals(rentalBill.getUtilityProofStatus(), UtilityProofStatusEnum.STATUS_3.getType())
                ? "凭证审核通过" : "凭证已驳回");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInitialBills(RentalContract rentalContract) {
        if (rentalContract == null) {
            return;
        }
        if (rentalContract.getDepositAmount() != null
                && rentalContract.getDepositAmount().compareTo(BigDecimal.ZERO) > 0
                && !existsBill(rentalContract.getId(), RentalBillTypeEnum.STATUS_1.getType(), null)) {
            RentalBill depositBill = new RentalBill();
            depositBill.setBillNo(generateBillNo());
            depositBill.setContractId(rentalContract.getId());
            depositBill.setHouseId(rentalContract.getHouseId());
            depositBill.setLandlordId(rentalContract.getLandlordId());
            depositBill.setTenantUserId(rentalContract.getTenantUserId());
            depositBill.setBillType(RentalBillTypeEnum.STATUS_1.getType());
            depositBill.setUtilityPaymentMode(rentalContract.getUtilityPaymentMode());
            depositBill.setRentAmount(BigDecimal.ZERO);
            depositBill.setDepositAmount(rentalContract.getDepositAmount());
            depositBill.setWaterAmount(BigDecimal.ZERO);
            depositBill.setElectricAmount(BigDecimal.ZERO);
            depositBill.setTotalAmount(rentalContract.getDepositAmount());
            depositBill.setDueDate(rentalContract.getStartDate());
            depositBill.setRemark("合同生效后自动生成的押金账单");
            depositBill.setUtilityProofStatus(UtilityProofStatusEnum.STATUS_5.getType());
            depositBill.setPayStatus(RentalBillPayStatusEnum.STATUS_1.getType());
            depositBill.setCreateTime(LocalDateTime.now());
            depositBill.setUpdateTime(LocalDateTime.now());
            save(depositBill);
        }

        String firstMonth = getBillMonth(rentalContract.getStartDate());
        if (!existsBill(rentalContract.getId(), RentalBillTypeEnum.STATUS_2.getType(), firstMonth)) {
            RentalBill rentBill = new RentalBill();
            rentBill.setBillNo(generateBillNo());
            rentBill.setContractId(rentalContract.getId());
            rentBill.setHouseId(rentalContract.getHouseId());
            rentBill.setLandlordId(rentalContract.getLandlordId());
            rentBill.setTenantUserId(rentalContract.getTenantUserId());
            rentBill.setBillType(RentalBillTypeEnum.STATUS_2.getType());
            rentBill.setBillMonth(firstMonth);
            rentBill.setUtilityPaymentMode(rentalContract.getUtilityPaymentMode());
            rentBill.setRentAmount(rentalContract.getMonthlyRent());
            rentBill.setDepositAmount(BigDecimal.ZERO);
            rentBill.setWaterAmount(BigDecimal.ZERO);
            rentBill.setElectricAmount(BigDecimal.ZERO);
            rentBill.setTotalAmount(rentalContract.getMonthlyRent());
            rentBill.setDueDate(rentalContract.getStartDate());
            rentBill.setRemark("合同生效后自动生成的首期租金账单");
            rentBill.setUtilityProofStatus(resolveProofStatus(rentalContract.getUtilityPaymentMode()));
            rentBill.setPayStatus(RentalBillPayStatusEnum.STATUS_1.getType());
            rentBill.setCreateTime(LocalDateTime.now());
            rentBill.setUpdateTime(LocalDateTime.now());
            save(rentBill);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPendingBillsByContractId(Integer contractId) {
        if (contractId == null) {
            return;
        }
        List<RentalBill> billList = this.baseMapper.listPendingByContractId(contractId);
        for (RentalBill rentalBill : billList) {
            rentalBill.setPayStatus(RentalBillPayStatusEnum.STATUS_3.getType());
            rentalBill.setUpdateTime(LocalDateTime.now());
            updateById(rentalBill);
        }
    }

    private void validateCreate(RentalBill rentalBill) {
        AssertUtils.notNull(rentalBill, "账单数据不能为空");
        AssertUtils.notNull(rentalBill.getContractId(), "合同不能为空");
        AssertUtils.hasText(rentalBill.getBillMonth(), "账单月份不能为空");
        AssertUtils.hasText(rentalBill.getDueDate(), "到期日期不能为空");
        RentalContract rentalContract = rentalContractMapper.selectById(rentalBill.getContractId());
        AssertUtils.notNull(rentalContract, "合同不存在");
        AssertUtils.equals(rentalContract.getStatus(), RentalContractStatusEnum.STATUS_2.getType(), "仅已生效合同可创建账单");
        if (rentalBill.getUtilityPaymentMode() == null) {
            rentalBill.setUtilityPaymentMode(rentalContract.getUtilityPaymentMode());
        }
        AssertUtils.isTrue(Objects.equals(rentalBill.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_1.getType())
                        || Objects.equals(rentalBill.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_2.getType()),
                "水电费支付方式非法");
        BigDecimal rentAmount = defaultAmount(rentalBill.getRentAmount(), rentalContract.getMonthlyRent());
        AssertUtils.isTrue(rentAmount.compareTo(BigDecimal.ZERO) > 0, "租金必须大于0");
        AssertUtils.isFalse(existsBill(rentalBill.getContractId(), RentalBillTypeEnum.STATUS_2.getType(), rentalBill.getBillMonth()),
                "该月份已存在租金账单");
        AssertUtils.notNegative(defaultAmount(rentalBill.getWaterAmount(), BigDecimal.ZERO), "水费不能小于0");
        AssertUtils.notNegative(defaultAmount(rentalBill.getElectricAmount(), BigDecimal.ZERO), "电费不能小于0");
    }

    private boolean existsBill(Integer contractId, Integer billType, String billMonth) {
        Integer count = this.baseMapper.countByContractAndType(contractId, billType, billMonth);
        return count != null && count > 0;
    }

    private RentalBill getByIdInternal(Integer id) {
        RentalBill rentalBill = this.baseMapper.selectById(id);
        AssertUtils.notNull(rentalBill, "账单不存在");
        return rentalBill;
    }

    private void assertBillAccess(RentalBillVO rentalBillVO) {
        boolean isAdmin = Objects.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole());
        boolean isTenant = Objects.equals(rentalBillVO.getTenantUserId(), LocalThreadHolder.getUserId());
        boolean isLandlord = Objects.equals(LocalThreadHolder.getUserId(), getLandlordUserId(rentalBillVO.getLandlordId()));
        AssertUtils.isTrue(isAdmin || isTenant || isLandlord, "非法操作");
    }

    private BigDecimal calculateTotalAmount(RentalBill rentalBill) {
        BigDecimal totalAmount = defaultAmount(rentalBill.getRentAmount(), BigDecimal.ZERO);
        if (Objects.equals(rentalBill.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_2.getType())) {
            totalAmount = totalAmount
                    .add(defaultAmount(rentalBill.getWaterAmount(), BigDecimal.ZERO))
                    .add(defaultAmount(rentalBill.getElectricAmount(), BigDecimal.ZERO));
        }
        return totalAmount;
    }

    private Integer resolveProofStatus(Integer utilityPaymentMode) {
        if (Objects.equals(utilityPaymentMode, UtilityPaymentModeEnum.STATUS_1.getType())) {
            return UtilityProofStatusEnum.STATUS_1.getType();
        }
        return UtilityProofStatusEnum.STATUS_5.getType();
    }

    private BigDecimal defaultAmount(BigDecimal source, BigDecimal defaultValue) {
        return source == null ? defaultValue : source;
    }

    private String getBillMonth(String date) {
        if (date == null || date.length() < 7) {
            return null;
        }
        return date.substring(0, 7);
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
        return landlord == null ? null : landlord.getUserId();
    }

    private String generateBillNo() {
        int random = new Random().nextInt(9000) + 1000;
        return "RB" + System.currentTimeMillis() + random;
    }
}
