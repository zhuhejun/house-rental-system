package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.AreaQueryDto;
import com.kmbeast.pojo.dto.UserAreaQueryDto;
import com.kmbeast.pojo.entity.Area;
import com.kmbeast.pojo.entity.UserArea;
import com.kmbeast.pojo.vo.UserAreaVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户常居住地持久化接口
 */
@Mapper
public interface UserAreaMapper extends BaseMapper<UserArea> {

    List<UserArea> list(UserAreaQueryDto UserAreaQueryDto);

    Integer listCount(UserAreaQueryDto UserAreaQueryDto);

    List<UserAreaVO> listUser(UserAreaQueryDto userAreaQueryDto);

}
