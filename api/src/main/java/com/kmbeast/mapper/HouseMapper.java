package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.AreaQueryDto;
import com.kmbeast.pojo.dto.HouseQueryDto;
import com.kmbeast.pojo.entity.Area;
import com.kmbeast.pojo.entity.House;
import com.kmbeast.pojo.vo.ChartVO;
import com.kmbeast.pojo.vo.HouseFlowIndexVO;
import com.kmbeast.pojo.vo.HouseListItemVO;
import com.kmbeast.pojo.vo.HouseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房屋持久化接口
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {

    List<HouseListItemVO> list(HouseQueryDto houseQueryDto);

    List<HouseListItemVO> listCreate(HouseQueryDto houseQueryDto);

    Integer listCount(HouseQueryDto houseQueryDto);

    HouseVO getById(@Param(value = "id") Integer id);

    List<Integer> selectIdsByLandlordId(@Param(value = "landlordId") Integer landlordId);

    List<HouseFlowIndexVO> flowIndexList(HouseQueryDto queryDto);

    List<ChartVO> cityHouseRange(@Param(value = "limitCount") Integer limitCount);

    List<Integer> getIds();


}
