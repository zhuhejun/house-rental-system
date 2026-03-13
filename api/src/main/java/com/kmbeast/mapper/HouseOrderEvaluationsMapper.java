package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.HouseOrderEvaluationsQueryDto;
import com.kmbeast.pojo.entity.HouseOrderEvaluations;
import com.kmbeast.pojo.vo.HouseOrderEvaluationsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预约看房评价持久化接口
 */
@Mapper
public interface HouseOrderEvaluationsMapper extends BaseMapper<HouseOrderEvaluations> {

    List<HouseOrderEvaluationsVO> list(HouseOrderEvaluationsQueryDto queryDto);

    Integer listCount(HouseOrderEvaluationsQueryDto queryDto);

}
