package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租赁合同查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RentalContractQueryDto extends QueryDto {

    private String keyword;

    private Integer landlordId;

    private Integer landlordUserId;

    private Integer tenantUserId;

    private Integer houseId;

    private Integer houseOrderInfoId;

    private Integer status;

    private Boolean tenantVisible;
}
