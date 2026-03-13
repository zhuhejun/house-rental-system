package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋类型枚举
 */
@Getter
@AllArgsConstructor
public enum HouseTypeEnum {

    APARTMENT(1, "公寓"),
    VILLA(2, "别墅"),
    BUNGALOW(3, "平房"),
    TOWNHOUSES(4, "联排别墅"),
    DETACHED_HOUSE(5, "独栋住宅"),
    COMMERCIAL_HOUSING(6, "商品房"),
    SELF_BUILT_HOUSE(7, "自建房");

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
        for (HouseTypeEnum value : HouseTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

}
