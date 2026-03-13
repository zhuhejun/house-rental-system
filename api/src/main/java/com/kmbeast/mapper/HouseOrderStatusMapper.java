package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.HouseOrderStatusQueryDto;
import com.kmbeast.pojo.entity.HouseOrderStatus;
import com.kmbeast.pojo.vo.HouseOrderStatusVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预约看房状态流转持久化接口
 */
@Mapper
public interface HouseOrderStatusMapper extends BaseMapper<HouseOrderStatus> {

    List<HouseOrderStatusVO> list(HouseOrderStatusQueryDto queryDto);

    Integer listCount(HouseOrderStatusQueryDto queryDto);

}
