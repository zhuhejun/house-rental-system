package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋租金枚举
 */
@Getter
@AllArgsConstructor
public enum HouseRentEnum {

    STATUS_1(1, "0-1000"),
    STATUS_2(2, "1000-1500"),
    STATUS_3(3, "1500-2500"),
    STATUS_4(4, "2500-10000"),
    STATUS_5(5, "10000-15000");
    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String detail;

}
