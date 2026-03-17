package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报修工单状态枚举
 */
@Getter
@AllArgsConstructor
public enum RepairOrderStatusEnum {

    STATUS_1(1, "待处理"),
    STATUS_2(2, "已受理"),
    STATUS_3(3, "处理中"),
    STATUS_4(4, "待租户确认"),
    STATUS_5(5, "已完成"),
    STATUS_6(6, "已取消"),
    STATUS_7(7, "已驳回");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (RepairOrderStatusEnum value : RepairOrderStatusEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }
}
