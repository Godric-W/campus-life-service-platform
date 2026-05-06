package com.god.common.exception;

import com.god.common.result.ResultCode;

public final class AssertUtil {

    private AssertUtil() {
    }

    public static void isTrue(boolean expression, ResultCode resultCode) {
        if (!expression) {
            throw new BusinessException(resultCode);
        }
    }

    public static void isFalse(boolean expression, ResultCode resultCode) {
        if (expression) {
            throw new BusinessException(resultCode);
        }
    }

    public static void notNull(Object object, ResultCode resultCode) {
        if (object == null) {
            throw new BusinessException(resultCode);
        }
    }

    public static void hasText(String text, ResultCode resultCode) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(resultCode);
        }
    }
}
