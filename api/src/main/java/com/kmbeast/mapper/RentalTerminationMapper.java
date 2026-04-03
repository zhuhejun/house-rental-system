package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.entity.RentalTermination;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退租申请持久层
 */
@Mapper
public interface RentalTerminationMapper extends BaseMapper<RentalTermination> {
}
