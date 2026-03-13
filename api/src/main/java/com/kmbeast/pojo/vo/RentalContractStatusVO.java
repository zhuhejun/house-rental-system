package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.RentalContractStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租赁合同状态流转展示对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RentalContractStatusVO extends RentalContractStatus {

    private String oldStatusText;

    private String newStatusText;
}
