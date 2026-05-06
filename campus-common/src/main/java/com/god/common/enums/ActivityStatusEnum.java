package com.god.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityStatusEnum {

    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    REGISTRATION_CLOSED("REGISTRATION_CLOSED", "报名截止"),
    FINISHED("FINISHED", "已结束"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;
}
