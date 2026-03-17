package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报修工单状态流转查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RepairOrderStatusQueryDto extends QueryDto {

    private Integer repairOrderId;
}
