package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.HouseOrderInfoQueryDto;
import com.kmbeast.pojo.entity.HouseOrderInfo;
import com.kmbeast.pojo.vo.HouseOrderInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预约看房持久化接口
 */
@Mapper
public interface HouseOrderInfoMapper extends BaseMapper<HouseOrderInfo> {

    /**
     * 通过房源ID查询其产生的所有的预约看房的订单ID列表
     *
     * @param houseId 房源ID
     * @return List<Integer> 预约看房的订单ID列表
     */
    List<Integer> getIdsByHouseId(@Param(value = "houseId") Integer houseId);

    List<HouseOrderInfoVO> list(HouseOrderInfoQueryDto houseOrderInfoQueryDto);

    Integer listCount(HouseOrderInfoQueryDto houseOrderInfoQueryDto);

}
