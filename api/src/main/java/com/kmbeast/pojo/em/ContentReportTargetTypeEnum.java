package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentReportTargetTypeEnum {

    COMMENT(1, "普通评论"),
    SERVICE_EVALUATION(2, "服务评价");

    private final Integer type;
    private final String name;

    public static boolean contains(Integer type) {
        if (type == null) {
            return false;
        }
        for (ContentReportTargetTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return true;
            }
        }
        return false;
    }
}
