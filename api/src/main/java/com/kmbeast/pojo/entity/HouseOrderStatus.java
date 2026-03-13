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
 * 房屋预约状态流转信息实体，与数据库的房屋预约状态流转信息信息表（house_order_status）对应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "house_order_status")
public class HouseOrderStatus {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 原始预约状态ID，外键，在枚举类里面进行定义
     */
    private Integer originStatus;
    /**
     * 新状态，外键，在枚举类里面进行定义
     */
    private Integer newId;
    /**
     * 房屋预约看房订单信息ID，外键
     */
    private Integer houseOrderInfoId;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
