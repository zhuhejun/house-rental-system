package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.AreaQueryDto;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.entity.Area;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.vo.LandlordVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 地区持久化接口
 */
@Mapper
public interface LandlordMapper extends BaseMapper<Landlord> {

    List<LandlordVO> list(LandlordQueryDto landlordQueryDto);

    Integer listCount(LandlordQueryDto landlordQueryDtos);

}
