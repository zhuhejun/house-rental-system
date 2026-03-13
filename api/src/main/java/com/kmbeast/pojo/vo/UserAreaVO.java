package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.UserArea;
import lombok.Data;

/**
 * 用户常居住地VO类
 */
@Data
public class UserAreaVO {
    /**
     * ID
     */
    private Integer id;
    /**
     * 省份ID
     */
    private Integer topAreaId;
    /**
     * 省份名
     */
    private String topAreaName;
    /**
     * 市区ID
     */
    private Integer cityAreaId;
    /**
     * 市区名称
     */
    private String cityAreaName;
}
