package com.god.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTypeEnum {

    SYSTEM("SYSTEM", "系统通知"),
    MARKET("MARKET", "二手交易通知"),
    TASK("TASK", "互助任务通知"),
    ACTIVITY("ACTIVITY", "活动通知");

    private final String code;
    private final String description;
}
