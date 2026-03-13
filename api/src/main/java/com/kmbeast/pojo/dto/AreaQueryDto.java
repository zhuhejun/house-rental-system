package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 地区查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AreaQueryDto extends QueryDto{
    /**
     * 父级ID
     */
    private Integer parentId;
    /**
     * 名称
     */
    private String name;
}
