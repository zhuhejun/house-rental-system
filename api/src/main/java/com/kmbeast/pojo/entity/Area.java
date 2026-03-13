package com.kmbeast.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地区实体，与数据库的地区表（area）对应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "area")
public class Area {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 父级ID
     */
    private Integer parentId;
    /**
     * 名称
     */
    private String name;

}
