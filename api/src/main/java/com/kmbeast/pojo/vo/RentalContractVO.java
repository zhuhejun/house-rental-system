package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.RentalContract;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租赁合同展示对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RentalContractVO extends RentalContract {

    private String houseName;

    private String houseCover;

    private String landlordName;

    private String tenantName;
}
