package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 预约看房评价信息查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseOrderEvaluationsQueryDto extends QueryDto{
    /**
     * 用户ID，外键，关联的是用户表，标识哪个用户做的评价
     */
    private Integer userId;
    /**
     * 房屋预约看房订单ID，外键，关联的是房屋预约看房信息表
     */
    private Integer houseOrderInfoId;
    /**
     * 房屋预约看房订单ID列表
     */
    private List<Integer> houseOrderInfoIds;
}
