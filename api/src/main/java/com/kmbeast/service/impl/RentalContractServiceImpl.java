package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.mapper.HouseOrderInfoMapper;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.mapper.RentalContractMapper;
import com.kmbeast.mapper.RentalContractStatusMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.dto.RentalContractQueryDto;
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
    private RentalBillService rentalBillService;

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
        LandlordVO landlord = getLandlord();
        queryDto.setLandlordId(landlord.getId());
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
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_4.getType(), rentalContract.getRejectReason());
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
        AssertUtils.isFalse(activeCount != null && activeCount > 0, "该房源已有其他生效中的合同");

        updateStatus(rentalContract, RentalContractStatusEnum.STATUS_3.getType(), null);
        rentalContract.setConfirmTime(LocalDateTime.now());
        rentalContract.setUpdateTime(LocalDateTime.now());
        updateById(rentalContract);

        House house = houseMapper.selectById(rentalContract.getHouseId());
        if (house != null) {
            house.setStatus(HouseStatusEnum.STATUS_3.getType());
            houseMapper.updateById(house);
        }
        rentalBillService.createInitialBills(rentalContract);
        return ApiResult.success("合同已确认生效");
    }

    @Override
    public Result<String> tenantReject(RentalContract rentalContract) {
        AssertUtils.notNull(rentalContract, "合同数据不能为空");
        AssertUtils.notNull(rentalContract.getId(), "合同ID不能为空");
        RentalContract dbContract = getEntityById(rentalContract.getId());
        AssertUtils.equals(dbContract.getTenantUserId(), LocalThreadHolder.getUserId(), "非法操作");
        AssertUtils.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_2.getType(), "当前状态无法拒绝");
        dbContract.setRejectReason(rentalContract.getRejectReason());
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_5.getType(), rentalContract.getRejectReason());
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
        AssertUtils.isFalse(Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_4.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_5.getType()),
                "当前状态无法取消");
        AssertUtils.isFalse(Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_6.getType())
                        || Objects.equals(dbContract.getStatus(), RentalContractStatusEnum.STATUS_7.getType()),
                "当前状态无法取消");

        dbContract.setCancelReason(rentalContract.getCancelReason());
        Integer beforeStatus = dbContract.getStatus();
        updateStatus(dbContract, RentalContractStatusEnum.STATUS_6.getType(), rentalContract.getCancelReason());
        dbContract.setUpdateTime(LocalDateTime.now());
        updateById(dbContract);
        rentalBillService.cancelPendingBillsByContractId(dbContract.getId());
        if (Objects.equals(beforeStatus, RentalContractStatusEnum.STATUS_3.getType())) {
            recoverHouseStatusIfNeeded(dbContract.getHouseId());
        }
        return ApiResult.success("合同已取消");
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
        AssertUtils.notNull(rentalContract.getDepositAmount(), "押金不能为空");
        AssertUtils.hasText(rentalContract.getPayCycle(), "付款方式不能为空");
        if (rentalContract.getUtilityPaymentMode() == null) {
            rentalContract.setUtilityPaymentMode(UtilityPaymentModeEnum.STATUS_2.getType());
        }
        AssertUtils.isTrue(Objects.equals(rentalContract.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_1.getType())
                        || Objects.equals(rentalContract.getUtilityPaymentMode(), UtilityPaymentModeEnum.STATUS_2.getType()),
                "水电费支付方式非法");
        AssertUtils.isTrue(rentalContract.getMonthlyRent().compareTo(BigDecimal.ZERO) > 0, "月租金必须大于0");
        AssertUtils.isTrue(rentalContract.getDepositAmount().compareTo(BigDecimal.ZERO) >= 0, "押金不能小于0");

        HouseOrderInfo orderInfo = houseOrderInfoMapper.selectById(rentalContract.getHouseOrderInfoId());
        AssertUtils.notNull(orderInfo, "预约记录不存在");
        AssertUtils.equals(orderInfo.getOrderStatus(), HouseOrderStatusEnum.STATUS_5.getType(), "仅已完成的预约可发起合同");

        Integer contractCount = this.baseMapper.countContractByOrderId(rentalContract.getHouseOrderInfoId());
        AssertUtils.isFalse(contractCount != null && contractCount > 0, "该预约已创建过合同");

        Integer activeCount = this.baseMapper.countActiveContractByHouseId(orderInfo.getHouseId(), null);
        AssertUtils.isFalse(activeCount != null && activeCount > 0, "该房源已有待确认或生效中的合同");
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

    private String generateContractNo() {
        int random = new Random().nextInt(9000) + 1000;
        return "RC" + System.currentTimeMillis() + random;
    }
}
