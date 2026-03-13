package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.RentalBillQueryDto;
import com.kmbeast.pojo.entity.RentalBill;
import com.kmbeast.pojo.vo.RentalBillVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租赁账单持久层
 */
@Mapper
public interface RentalBillMapper extends BaseMapper<RentalBill> {

    List<RentalBillVO> list(RentalBillQueryDto queryDto);

    Integer listCount(RentalBillQueryDto queryDto);

    RentalBillVO getById(@Param(value = "id") Integer id);

    Integer countByContractAndType(@Param(value = "contractId") Integer contractId,
                                   @Param(value = "billType") Integer billType,
                                   @Param(value = "billMonth") String billMonth);

    List<RentalBill> listPendingByContractId(@Param(value = "contractId") Integer contractId);
}
