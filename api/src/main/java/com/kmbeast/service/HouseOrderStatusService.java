package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderStatusQueryDto;
import com.kmbeast.pojo.entity.HouseOrderStatus;
import com.kmbeast.pojo.vo.HouseOrderStatusVO;

import java.util.List;

/**
 * 预约看房状态流转业务逻辑接口
 */
public interface HouseOrderStatusService extends IService<HouseOrderStatus> {

    Result<List<HouseOrderStatusVO>> list(HouseOrderStatusQueryDto queryDto);

    Result<String> saveEntity(HouseOrderStatus houseOrderStatus);

}
