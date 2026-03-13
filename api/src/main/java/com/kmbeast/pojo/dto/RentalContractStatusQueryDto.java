package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租赁合同状态流转查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RentalContractStatusQueryDto extends QueryDto {

    private Integer rentalContractId;
}
