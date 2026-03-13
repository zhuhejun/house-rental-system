package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋面积枚举
 */
@Getter
@AllArgsConstructor
public enum HouseSizeNumberEnum {

    STATUS_1(1, "0-50"),
    STATUS_2(2, "50-150"),
    STATUS_3(3, "150-250"),
    STATUS_4(4, "250-1000"),
    STATUS_5(5, "1000-10000");
    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String detail;

}
