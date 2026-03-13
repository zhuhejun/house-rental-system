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
 * 房屋预约看房订单实体，与数据库的房屋预约看房订单信息表（house_order_info）对应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "house_order_info")
public class HouseOrderInfo {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 用户ID，外键，关联的是用户表，标识哪个用户做的预约
     */
    private Integer userId;
    /**
     * 房屋ID，外键，关联的是房屋信息表，标识用户预约看房的是哪个房源
     */
    private Integer houseId;
    /**
     * 预约日期
     */
    private String orderDate;
    /**
     * 预约时间段
     */
    private String orderTimeSplit;
    /**
     * 预约状态（1：预约中；2：已预约；3：预约失败；4：已取消；5：已完成）
     */
    private Integer orderStatus;
    /**
     * 拒绝原由
     */
    private String rejectCause;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
