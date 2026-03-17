package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.mapper.RepairOrderStatusMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RepairOrderStatusQueryDto;
import com.kmbeast.pojo.em.RepairOrderStatusEnum;
import com.kmbeast.pojo.entity.RepairOrderStatus;
import com.kmbeast.pojo.vo.RepairOrderStatusVO;
import com.kmbeast.service.RepairOrderStatusService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 报修工单状态流转业务实现
 */
@Service
public class RepairOrderStatusServiceImpl extends ServiceImpl<RepairOrderStatusMapper, RepairOrderStatus>
        implements RepairOrderStatusService {

    @Override
    public Result<List<RepairOrderStatusVO>> list(RepairOrderStatusQueryDto queryDto) {
        List<RepairOrderStatusVO> statusVOS = this.baseMapper.list(queryDto);
        for (RepairOrderStatusVO statusVO : statusVOS) {
            statusVO.setOldStatusText(RepairOrderStatusEnum.getDetail(statusVO.getOriginStatus()));
            statusVO.setNewStatusText(RepairOrderStatusEnum.getDetail(statusVO.getNewId()));
        }
        Integer count = this.baseMapper.listCount(queryDto);
        return ApiResult.success(statusVOS, count);
    }
}
