package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RentalBillQueryDto;
import com.kmbeast.pojo.entity.RentalBill;
import com.kmbeast.pojo.entity.RentalContract;
import com.kmbeast.pojo.vo.RentalBillVO;

import java.util.List;

/**
 * 账单业务
 */
public interface RentalBillService extends IService<RentalBill> {

    Result<String> saveEntity(RentalBill rentalBill);

    Result<List<RentalBillVO>> listUser(RentalBillQueryDto queryDto);

    Result<List<RentalBillVO>> listLandlord(RentalBillQueryDto queryDto);

    Result<List<RentalBillVO>> list(RentalBillQueryDto queryDto);

    Result<RentalBillVO> getById(Integer id);

    Result<String> submitUtilityProof(RentalBill rentalBill);

    Result<String> auditUtilityProof(RentalBill rentalBill);

    void createDepositBill(RentalContract rentalContract);

    void createFirstRentBill(RentalContract rentalContract);

    void cancelPendingBillsByContractId(Integer contractId);
}
