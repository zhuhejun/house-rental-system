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
 * 退租申请实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "rental_termination")
public class RentalTermination {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer rentalContractId;

    private Integer houseId;

    private Integer landlordId;

    private Integer tenantUserId;

    private Integer applyUserId;

    private String applyReason;

    private Integer counterpartyUserId;

    private String counterpartyRejectReason;

    private BigDecimal refundAmount;

    private String settlementVoucherUrl;

    private String settlementVoucherNote;

    private String adminAuditNote;

    private String refundVoucherUrl;

    private String refundVoucherNote;

    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime counterpartyDecisionTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime adminAuditTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
