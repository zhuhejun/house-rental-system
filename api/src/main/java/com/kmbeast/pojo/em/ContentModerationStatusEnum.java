package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentModerationStatusEnum {

    NORMAL(1, "正常"),
    PENDING_REVIEW(2, "待审核"),
    BLOCKED(3, "已屏蔽");

    private final Integer type;
    private final String name;
}
