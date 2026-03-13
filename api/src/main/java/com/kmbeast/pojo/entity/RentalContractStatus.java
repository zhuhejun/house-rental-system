package com.kmbeast.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租赁合同状态流转实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "rental_contract_status")
public class RentalContractStatus {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer originStatus;

    private Integer newId;

    private Integer rentalContractId;

    private Integer operatorId;

    private String note;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
