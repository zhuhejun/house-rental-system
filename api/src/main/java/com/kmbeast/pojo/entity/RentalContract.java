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
 * 租赁合同实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "rental_contract")
public class RentalContract {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String contractNo;

    private String title;

    private Integer houseId;

    private Integer landlordId;

    private Integer tenantUserId;

    private Integer houseOrderInfoId;

    private String startDate;

    private String endDate;

    private BigDecimal monthlyRent;

    private Integer depositMethodId;

    private BigDecimal depositAmount;

    private String payCycle;

    private Integer utilityPaymentMode;

    private String contractContent;

    private String attachmentUrl;

    private Integer status;

    private String rejectReason;

    private String cancelReason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
