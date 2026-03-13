package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.RentalContractStatusQueryDto;
import com.kmbeast.pojo.entity.RentalContractStatus;
import com.kmbeast.pojo.vo.RentalContractStatusVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 租赁合同状态流转持久层
 */
@Mapper
public interface RentalContractStatusMapper extends BaseMapper<RentalContractStatus> {

    List<RentalContractStatusVO> list(RentalContractStatusQueryDto queryDto);

    Integer listCount(RentalContractStatusQueryDto queryDto);
}
