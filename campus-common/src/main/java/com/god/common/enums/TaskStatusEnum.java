package com.god.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskStatusEnum {

    PUBLISHED("PUBLISHED", "已发布，待接单"),
    ACCEPTED("ACCEPTED", "已接单，进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;
}
