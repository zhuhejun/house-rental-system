package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.RepairOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报修工单展示对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RepairOrderVO extends RepairOrder {

    private String repairTypeText;

    private String houseName;

    private String houseCover;

    private String landlordName;

    private String tenantName;

    private String contractNo;
}
