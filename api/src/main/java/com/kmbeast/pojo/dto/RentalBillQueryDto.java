package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账单查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RentalBillQueryDto extends QueryDto {

    private String keyword;

    private Integer contractId;

    private Integer landlordId;

    private Integer tenantUserId;

    private Integer billType;

    private Integer payStatus;

    private Integer utilityPaymentMode;

    private Integer utilityProofStatus;
}
