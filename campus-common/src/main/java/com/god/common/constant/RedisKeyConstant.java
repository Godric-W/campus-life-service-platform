package com.god.common.constant;

public final class RedisKeyConstant {

    public static final String LOGIN_CAPTCHA_PREFIX = "campus:auth:captcha:";
    public static final String TOKEN_BLACKLIST_PREFIX = "campus:auth:token:blacklist:";
    public static final String USER_INFO_PREFIX = "campus:user:info:";
    public static final String MARKET_ITEM_DETAIL_PREFIX = "campus:market:item:";
    public static final String ACTIVITY_DETAIL_PREFIX = "campus:activity:detail:";

    private RedisKeyConstant() {
    }
}
