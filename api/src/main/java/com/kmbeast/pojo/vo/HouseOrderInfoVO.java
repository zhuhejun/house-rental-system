package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.HouseOrderInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约看房VO类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseOrderInfoVO extends HouseOrderInfo {

    /**
     * 用户名
     */
    private String username;
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 房源名
     */
    private String houseName;
    /**
     * 房源封面
     */
    private String cover;

}
