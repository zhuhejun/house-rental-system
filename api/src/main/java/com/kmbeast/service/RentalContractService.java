package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RentalContractQueryDto;
import com.kmbeast.pojo.entity.RentalContract;
import com.kmbeast.pojo.vo.RentalContractVO;

import java.util.List;

/**
 * 租赁合同业务接口
 */
public interface RentalContractService extends IService<RentalContract> {

    Result<String> saveEntity(RentalContract rentalContract);

    Result<List<RentalContractVO>> list(RentalContractQueryDto queryDto);

    Result<List<RentalContractVO>> listUser(RentalContractQueryDto queryDto);

    Result<List<RentalContractVO>> listLandlord(RentalContractQueryDto queryDto);

    Result<RentalContractVO> getById(Integer id);

    Result<String> adminApprove(Integer id);

    Result<String> adminReject(RentalContract rentalContract);

    Result<String> tenantConfirm(Integer id);

    Result<String> tenantReject(RentalContract rentalContract);

    Result<String> cancel(RentalContract rentalContract);
}
