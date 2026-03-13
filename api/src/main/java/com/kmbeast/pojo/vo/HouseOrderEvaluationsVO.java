package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.HouseOrderEvaluations;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约看房评价VO类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseOrderEvaluationsVO extends HouseOrderEvaluations {
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户头像
     */
    private String avatar;
}
