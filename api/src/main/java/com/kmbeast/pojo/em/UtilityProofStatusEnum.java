package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 水电凭证状态
 */
@Getter
@AllArgsConstructor
public enum UtilityProofStatusEnum {

    STATUS_1(1, "待提交"),
    STATUS_2(2, "待审核"),
    STATUS_3(3, "已通过"),
    STATUS_4(4, "已驳回"),
    STATUS_5(5, "无需凭证");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (UtilityProofStatusEnum value : values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "未知";
    }
}
