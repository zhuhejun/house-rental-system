package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否审核枚举
 */
@Getter
@AllArgsConstructor
public enum IsAuditEnum {

    STATUS_1(true, "已审核"),
    STATUS_2(false, "未审核");

    private final Boolean type;
    private final String detail;

}
