package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.Landlord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房东信息VO类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LandlordVO extends Landlord {
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 用户名
     */
    private String username;
}
