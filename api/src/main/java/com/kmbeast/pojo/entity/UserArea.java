package com.kmbeast.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户常居住地信息实体，与数据库的用户常居住地信息表（user_area）对应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_area")
public class UserArea {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 用户ID，外键，关联的是用户信息表
     */
    private Integer userId;
    /**
     * 地区ID，外键，关联的是地区信息表
     */
    private Integer areaId;

}
