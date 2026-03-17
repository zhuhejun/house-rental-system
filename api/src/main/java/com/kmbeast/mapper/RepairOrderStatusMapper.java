package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.RepairOrderStatusQueryDto;
import com.kmbeast.pojo.entity.RepairOrderStatus;
import com.kmbeast.pojo.vo.RepairOrderStatusVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 报修工单状态流转持久层
 */
@Mapper
public interface RepairOrderStatusMapper extends BaseMapper<RepairOrderStatus> {

    List<RepairOrderStatusVO> list(RepairOrderStatusQueryDto queryDto);

    Integer listCount(RepairOrderStatusQueryDto queryDto);
}
