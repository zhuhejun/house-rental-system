package com.kmbeast.pojo.vo;

import lombok.Data;

/**
 * 评分的VO类
 */
@Data
public class ScoreVO {
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 内容ID
     */
    private Integer contentId;
    /**
     * 评分
     */
    private Double score;
}
