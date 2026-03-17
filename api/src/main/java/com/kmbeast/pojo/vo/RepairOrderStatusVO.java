package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.RepairOrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报修工单状态流转展示对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RepairOrderStatusVO extends RepairOrderStatus {

    private String oldStatusText;

    private String newStatusText;
}
