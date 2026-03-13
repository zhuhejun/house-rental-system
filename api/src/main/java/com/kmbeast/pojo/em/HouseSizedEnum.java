package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋户型枚举
 */
@Getter
@AllArgsConstructor
public enum HouseSizedEnum {

    HOUSE_SIZED_1(1, "一室一厅"),
    HOUSE_SIZED_2(2, "两室一厅"),
    HOUSE_SIZED_3(3, "三室一厅"),
    HOUSE_SIZED_4(4, "大单间"),
    HOUSE_SIZED_5(5, "四室一厅"),
    HOUSE_SIZED_6(6, "一栋");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String detail;

    /**
     * 通过状态获取状态描述
     *
     * @param type 状态
     * @return String
     */
    public static String getDetail(Integer type) {
        for (HouseSizedEnum value : HouseSizedEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

}
