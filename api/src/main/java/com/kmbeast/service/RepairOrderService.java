package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RepairOrderQueryDto;
import com.kmbeast.pojo.entity.RepairOrder;
import com.kmbeast.pojo.vo.RepairOrderVO;

import java.util.List;

/**
 * 报修工单业务接口
 */
public interface RepairOrderService extends IService<RepairOrder> {

    Result<String> saveEntity(RepairOrder repairOrder);

    Result<List<RepairOrderVO>> list(RepairOrderQueryDto queryDto);

    Result<List<RepairOrderVO>> listUser(RepairOrderQueryDto queryDto);

    Result<List<RepairOrderVO>> listLandlord(RepairOrderQueryDto queryDto);

    Result<RepairOrderVO> getById(Integer id);

    Result<String> landlordAccept(Integer id);

    Result<String> landlordProcessing(Integer id);

    Result<String> landlordFinish(RepairOrder repairOrder);

    Result<String> tenantConfirmFinish(Integer id);

    Result<String> tenantCancel(RepairOrder repairOrder);

    Result<String> adminReject(RepairOrder repairOrder);
}
