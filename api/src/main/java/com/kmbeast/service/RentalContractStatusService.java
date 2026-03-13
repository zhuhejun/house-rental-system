package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RentalContractStatusQueryDto;
import com.kmbeast.pojo.entity.RentalContractStatus;
import com.kmbeast.pojo.vo.RentalContractStatusVO;

import java.util.List;

/**
 * 租赁合同状态流转业务接口
 */
public interface RentalContractStatusService extends IService<RentalContractStatus> {

    Result<List<RentalContractStatusVO>> list(RentalContractStatusQueryDto queryDto);
}
