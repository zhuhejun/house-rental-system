package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 生活设施枚举
 */
@Getter
@AllArgsConstructor
public enum LivingFacilitiesEnum {

    TYPE_1("冰箱", false),
    TYPE_2("洗衣机", false),
    TYPE_3("热水器", false),
    TYPE_4("宽带", false),
    TYPE_5("沙发", false),
    TYPE_6("油烟机", false),
    TYPE_7("燃气灶", false),
    TYPE_8("可做饭", false),
    TYPE_9("电视", false),
    TYPE_10("空调", false),
    TYPE_11("衣柜", false),
    TYPE_12("床", false),
    TYPE_13("卫生间", false),
    TYPE_14("智能门锁", false),
    TYPE_15("阳台", false),
    TYPE_16("暖气", false),
    TYPE_17("橱柜", false);

    /**
     * 类型
     */
    private final String type;
    /**
     * 选中状态
     */
    private final Boolean selected;

}
