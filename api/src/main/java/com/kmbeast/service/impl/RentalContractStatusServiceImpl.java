package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.mapper.RentalContractStatusMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RentalContractStatusQueryDto;
import com.kmbeast.pojo.em.RentalContractStatusEnum;
import com.kmbeast.pojo.entity.RentalContractStatus;
import com.kmbeast.pojo.vo.RentalContractStatusVO;
import com.kmbeast.service.RentalContractStatusService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租赁合同状态流转业务实现
 */
@Service
public class RentalContractStatusServiceImpl extends ServiceImpl<RentalContractStatusMapper, RentalContractStatus>
        implements RentalContractStatusService {

    @Override
    public Result<List<RentalContractStatusVO>> list(RentalContractStatusQueryDto queryDto) {
        List<RentalContractStatusVO> statusVOS = this.baseMapper.list(queryDto);
        for (RentalContractStatusVO statusVO : statusVOS) {
            statusVO.setOldStatusText(RentalContractStatusEnum.getDetail(statusVO.getOriginStatus()));
            statusVO.setNewStatusText(RentalContractStatusEnum.getDetail(statusVO.getNewId()));
        }
        Integer count = this.baseMapper.listCount(queryDto);
        return ApiResult.success(statusVOS, count);
    }
}
