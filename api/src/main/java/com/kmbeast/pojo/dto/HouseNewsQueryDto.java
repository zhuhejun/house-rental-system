package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 房屋资讯信息查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseNewsQueryDto extends QueryDto{
    /**
     * 标题
     */
    private String title;
    /**
     * 房屋资讯ID列表
     */
    private List<Integer> ids;
}
