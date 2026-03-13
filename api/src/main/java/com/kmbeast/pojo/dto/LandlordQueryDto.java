package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房东信息查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LandlordQueryDto extends QueryDto{
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 审核状态（0：未审核；1：已审核）
     */
    private Boolean isAudit;
    /**
     * 身份证号
     */
    private String idcard;
}
