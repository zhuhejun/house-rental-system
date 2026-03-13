package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋租赁类型枚举
 */
@Getter
@AllArgsConstructor
public enum RentalTypeEnum {

    TOTAL(1, "整租"),
    TOGETHER(2, "合租");

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
        for (RentalTypeEnum value : RentalTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

}
