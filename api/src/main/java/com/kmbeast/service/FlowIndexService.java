package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.FlowIndexQueryDto;
import com.kmbeast.pojo.entity.FlowIndex;
import com.kmbeast.pojo.vo.HouseListItemVO;
import com.kmbeast.pojo.vo.HouseNewsListVO;

import java.util.List;

/**
 * 流量指标业务逻辑接口
 */
public interface FlowIndexService extends IService<FlowIndex> {

    Result<List<FlowIndex>> list(FlowIndexQueryDto flowIndexQueryDto);

    Result<String> saveEntity(FlowIndex flowIndex);

    Result<String> viewOperation(FlowIndex flowIndex);

    Result<String> saveOperation(FlowIndex flowIndex);

    Result<String> stayOperation(FlowIndex flowIndex);

    Result<List<HouseListItemVO>> saveListHouse(FlowIndexQueryDto flowIndexQueryDto);

    Result<List<HouseNewsListVO>> saveListHouseNews(FlowIndexQueryDto flowIndexQueryDto);


}
