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
 * 流量状况实体，与数据库的流量状况表（flow_index）对应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "flow_index")
public class FlowIndex {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 流量类型（1：浏览；2：收藏；3：停留；4：展现）
     */
    private Integer type;
    /**
     * 停留时长（毫秒）
     */
    private Integer times;
    /**
     * 内容模块（标识的是该流量属于哪一个模块下面）
     */
    private String contentType;
    /**
     * 内容ID（标识的是该模块下面的哪一个内容的ID）
     */
    private Integer contentId;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
