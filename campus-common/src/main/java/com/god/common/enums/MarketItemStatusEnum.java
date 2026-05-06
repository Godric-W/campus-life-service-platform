package com.god.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarketItemStatusEnum {

    ON_SALE("ON_SALE", "在售"),
    LOCKED("LOCKED", "已预定"),
    SOLD("SOLD", "已售出"),
    OFF_SHELF("OFF_SHELF", "已下架");

    private final String code;
    private final String description;
}
