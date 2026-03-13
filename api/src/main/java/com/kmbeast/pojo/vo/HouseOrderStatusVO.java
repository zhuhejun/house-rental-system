package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.HouseOrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约看房状态流转VO类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseOrderStatusVO extends HouseOrderStatus {

    /**
     * 旧状态描述
     */
    private String oldStatusText;
    /**
     * 新状态描述
     */
    private String newStatusText;

}
