package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋装修状态枚举
 */
@Getter
@AllArgsConstructor
public enum HouseFitmentEnum {

    HOUSE_FITMENT_1(1, "毛坯房"),
    HOUSE_FITMENT_2(2, "简装"),
    HOUSE_FITMENT_3(3, "精装修");

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
        for (HouseFitmentEnum value : HouseFitmentEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

}
