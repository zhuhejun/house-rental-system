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
 * 报修工单实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "repair_order")
public class RepairOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String repairNo;

    private Integer contractId;

    private Integer houseId;

    private Integer landlordId;

    private Integer tenantUserId;

    private String title;

    private Integer repairType;

    private String content;

    private Integer isUrgent;

    private String expectVisitTime;

    private String attachmentUrl;

    private Integer status;

    private String handleNote;

    private String handleVoucherUrl;

    private BigDecimal repairAmount;

    private Integer paymentStatus;

    private String cancelReason;

    private String rejectReason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tenantConfirmTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
