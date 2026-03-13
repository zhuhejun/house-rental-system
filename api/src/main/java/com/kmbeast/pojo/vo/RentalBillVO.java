package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.RentalBill;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租赁账单展示对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RentalBillVO extends RentalBill {

    private String contractNo;

    private String contractTitle;

    private String houseName;

    private String houseCover;

    private String landlordName;

    private String tenantName;
}
