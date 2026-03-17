package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报修工单查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RepairOrderQueryDto extends QueryDto {

    private String keyword;

    private Integer landlordId;

    private Integer landlordUserId;

    private Integer tenantUserId;

    private Integer contractId;

    private Integer houseId;

    private Integer status;

    private Integer paymentStatus;
}
