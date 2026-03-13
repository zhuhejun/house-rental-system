package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋押金枚举
 */
@Getter
@AllArgsConstructor
public enum HouseDepositEnum {

    HOUSE_SIZED_1(1, "押一付一"),
    HOUSE_SIZED_2(2, "押一付三"),
    HOUSE_SIZED_3(3, "押二付一"),
    HOUSE_SIZED_4(4, "押二付三");

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
        for (HouseDepositEnum value : HouseDepositEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

    public static Integer getDepositMonths(Integer type) {
        if (type == null) {
            return 0;
        }
        switch (type) {
            case 1:
            case 2:
                return 1;
            case 3:
            case 4:
                return 2;
            default:
                return 0;
        }
    }

    public static Integer getPayMonths(Integer type) {
        if (type == null) {
            return 0;
        }
        switch (type) {
            case 1:
            case 3:
                return 1;
            case 2:
            case 4:
                return 3;
            default:
                return 0;
        }
    }

}
