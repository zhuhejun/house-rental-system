package com.kmbeast.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租赁账单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "rental_bill")
public class RentalBill {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String billNo;

    private Integer contractId;

    private Integer houseId;

    private Integer landlordId;

    private Integer tenantUserId;

    private Integer billType;

    private String billMonth;

    private Integer utilityPaymentMode;

    private BigDecimal rentAmount;

    private BigDecimal depositAmount;

    private BigDecimal waterAmount;

    private BigDecimal electricAmount;

    private BigDecimal totalAmount;

    private String dueDate;

    private String remark;

    private String utilityProofUrl;

    private Integer utilityProofStatus;

    private String utilityProofNote;

    private Integer payStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
