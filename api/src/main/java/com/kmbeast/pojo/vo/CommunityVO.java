package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.Community;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小区信息VO类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommunityVO extends Community {
    /**
     * 省份ID
     */
    private Integer topAreaId;
    /**
     * 省份名
     */
    private String topAreaName;
    /**
     * 市区名
     */
    private String cityAreaName;
}
