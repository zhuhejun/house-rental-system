package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.FlowIndexQueryDto;
import com.kmbeast.pojo.entity.FlowIndex;
import com.kmbeast.pojo.vo.HouseFlowIndexVO;
import com.kmbeast.pojo.vo.LandlordVO;
import com.kmbeast.pojo.vo.ScoreVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 流量指标持久化接口
 */
@Mapper
public interface FlowIndexMapper extends BaseMapper<FlowIndex> {

    List<ScoreVO> getScores(FlowIndexQueryDto flowIndexQueryDto);

    List<FlowIndex> list(FlowIndexQueryDto flowIndexQueryDto);

    Integer listCount(FlowIndexQueryDto flowIndexQueryDto);

}
