package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.RentalContractQueryDto;
import com.kmbeast.pojo.entity.RentalContract;
import com.kmbeast.pojo.vo.RentalContractVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租赁合同持久层
 */
@Mapper
public interface RentalContractMapper extends BaseMapper<RentalContract> {

    List<RentalContractVO> list(RentalContractQueryDto queryDto);

    Integer listCount(RentalContractQueryDto queryDto);

    RentalContractVO getById(@Param(value = "id") Integer id);

    Integer countActiveContractByHouseId(@Param(value = "houseId") Integer houseId,
                                         @Param(value = "excludeId") Integer excludeId);

    Integer countContractByOrderId(@Param(value = "houseOrderInfoId") Integer houseOrderInfoId);
}
