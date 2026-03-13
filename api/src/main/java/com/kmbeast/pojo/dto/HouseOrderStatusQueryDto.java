package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约看房状态信息查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseOrderStatusQueryDto extends QueryDto{
    /**
     * 房屋预约看房订单信息ID，外键
     */
    private Integer houseOrderInfoId;
}
