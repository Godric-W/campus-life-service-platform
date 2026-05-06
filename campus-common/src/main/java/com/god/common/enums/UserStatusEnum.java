package com.god.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatusEnum {

    NORMAL(1, "正常"),
    DISABLED(0, "禁用");

    private final Integer code;
    private final String description;
}
