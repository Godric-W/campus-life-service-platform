package com.god.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRoleEnum {

    STUDENT("STUDENT", "普通学生"),
    CLUB_ADMIN("CLUB_ADMIN", "社团管理员"),
    ADMIN("ADMIN", "系统管理员");

    private final String code;
    private final String description;
}
