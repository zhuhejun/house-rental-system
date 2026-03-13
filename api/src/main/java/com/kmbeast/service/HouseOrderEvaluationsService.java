package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderEvaluationsQueryDto;
import com.kmbeast.pojo.entity.HouseOrderEvaluations;
import com.kmbeast.pojo.vo.HouseOrderEvaluationsVO;

import java.util.List;

/**
 * 预约看房评价业务逻辑接口
 */
public interface HouseOrderEvaluationsService extends IService<HouseOrderEvaluations> {

    Result<List<HouseOrderEvaluationsVO>> list(HouseOrderEvaluationsQueryDto queryDto);

    Result<String> saveEntity(HouseOrderEvaluations houseOrderEvaluations);

    Result<List<HouseOrderEvaluationsVO>> houseList(Integer houseId);

}
