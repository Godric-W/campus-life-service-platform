package com.god.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源状态冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_SERVER_ERROR(500, "系统异常，请稍后再试"),

    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    USER_DISABLED(1002, "用户已被禁用"),
    USER_NOT_FOUND(1003, "用户不存在"),
    TOKEN_INVALID(1004, "Token 无效"),

    MARKET_ITEM_NOT_FOUND(2001, "商品不存在"),
    MARKET_ITEM_STATUS_ERROR(2002, "商品状态不允许该操作"),

    TASK_NOT_FOUND(3001, "任务不存在"),
    TASK_STATUS_ERROR(3002, "任务状态不允许该操作"),

    ACTIVITY_NOT_FOUND(4001, "活动不存在"),
    ACTIVITY_REGISTRATION_CLOSED(4002, "活动报名已截止"),
    ACTIVITY_PARTICIPANTS_FULL(4003, "活动人数已满"),

    NOTIFICATION_NOT_FOUND(5001, "通知不存在");

    private final Integer code;
    private final String message;
}
