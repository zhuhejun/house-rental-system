package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderInfoQueryDto;
import com.kmbeast.pojo.entity.HouseOrderInfo;
import com.kmbeast.pojo.vo.HouseOrderInfoVO;

import java.util.List;

/**
 * 预约看房业务逻辑接口
 */
public interface HouseOrderInfoService extends IService<HouseOrderInfo> {

    Result<List<HouseOrderInfoVO>> list(HouseOrderInfoQueryDto houseOrderInfoQueryDto);

    Result<String> saveEntity(HouseOrderInfo houseOrderInfo);

    Result<String> updateEntity(HouseOrderInfo houseOrderInfo);

    Result<List<HouseOrderInfoVO>> listLandlord(HouseOrderInfoQueryDto houseOrderInfoQueryDto);

}
