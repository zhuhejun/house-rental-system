package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋朝向枚举
 */
@Getter
@AllArgsConstructor
public enum HouseDirectionEnum {

    EAST(1, "东"),
    WEST(2, "西"),
    SOUTH(3, "南"),
    NORTH(4, "北"),
    DIRECTION_1(5, "坐北朝南"),
    DIRECTION_2(6, "南北通透");

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
        for (HouseDirectionEnum value : HouseDirectionEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

}
