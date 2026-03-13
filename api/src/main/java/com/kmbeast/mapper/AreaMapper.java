package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.AreaQueryDto;
import com.kmbeast.pojo.entity.Area;
import com.kmbeast.pojo.entity.UserArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地区持久化接口
 */
@Mapper
public interface AreaMapper extends BaseMapper<Area> {

    List<Area> list(AreaQueryDto areaQueryDto);

    Integer listCount(AreaQueryDto areaQueryDto);

}
