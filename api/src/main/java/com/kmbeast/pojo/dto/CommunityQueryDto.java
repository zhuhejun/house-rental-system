package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小区查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommunityQueryDto extends QueryDto {
    /**
     * 地区ID
     */
    private Integer areaId;
    /**
     * 名称
     */
    private String name;
}
