package com.kmbeast.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_report")
public class ContentReport {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer targetType;

    private Integer targetId;

    private Integer reporterId;

    private String reason;

    /**
     * 处理状态（1：待处理；2：已处理）
     */
    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
