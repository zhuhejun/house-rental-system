package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RepairOrderStatusQueryDto;
import com.kmbeast.pojo.entity.RepairOrderStatus;
import com.kmbeast.pojo.vo.RepairOrderStatusVO;

import java.util.List;

/**
 * 报修工单状态流转业务接口
 */
public interface RepairOrderStatusService extends IService<RepairOrderStatus> {

    Result<List<RepairOrderStatusVO>> list(RepairOrderStatusQueryDto queryDto);
}
