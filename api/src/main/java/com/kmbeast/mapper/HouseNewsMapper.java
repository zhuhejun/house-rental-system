package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.HouseNewsQueryDto;
import com.kmbeast.pojo.entity.HouseNews;
import com.kmbeast.pojo.vo.HouseNewsListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 房屋资讯持久化接口
 */
@Mapper
public interface HouseNewsMapper extends BaseMapper<HouseNews> {

    List<Integer> getIds();

    List<HouseNewsListVO> list(HouseNewsQueryDto houseNewsQueryDto);

    Integer listCount(HouseNewsQueryDto houseNewsQueryDto);

}
