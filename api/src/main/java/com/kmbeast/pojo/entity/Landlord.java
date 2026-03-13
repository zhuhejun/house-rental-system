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
 * 房东信息实体，与数据库的房东信息表（landlord）对应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "landlord")
public class Landlord {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 申请人ID，外键，关联的是用户表，标识是由哪个用户进行的申请
     */
    private Integer userId;
    /**
     * 身份证正面照
     */
    private String idcardFront;
    /**
     * 身份证反面照
     */
    private String idcardOpposite;
    /**
     * 身份证号
     */
    private String idcard;
    /**
     * 审核状态（0：未审核；1：已审核）
     */
    private Boolean isAudit;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
