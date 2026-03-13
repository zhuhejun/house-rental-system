package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 预约看房信息查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseOrderInfoQueryDto extends QueryDto {
    /**
     * 用户ID，外键，关联的是用户表，标识哪个用户做的预约
     */
    private Integer userId;
    /**
     * 房屋ID，外键，关联的是房屋信息表，标识用户预约看房的是哪个房源
     */
    private Integer houseId;
    /**
     * 预约日期
     */
    private String orderDate;
    /**
     * 预约状态（1：预约中；2：已预约；3：预约失败；4：已取消；5：已完成）
     */
    private Integer orderStatus;
    /**
     * 预约时间段
     */
    private String orderTimeSplit;
    /**
     * 房屋ID集合
     */
    private List<Integer> houseIds;
}
