package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户常居住地查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserAreaQueryDto extends QueryDto{
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 地区ID
     */
    private Integer areaId;
}
