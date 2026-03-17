package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.RepairOrderQueryDto;
import com.kmbeast.pojo.entity.RepairOrder;
import com.kmbeast.pojo.vo.RepairOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报修工单持久层
 */
@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    List<RepairOrderVO> list(RepairOrderQueryDto queryDto);

    Integer listCount(RepairOrderQueryDto queryDto);

    RepairOrderVO getById(@Param(value = "id") Integer id);
}
