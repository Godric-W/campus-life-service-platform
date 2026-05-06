package com.god.common.utils;

import com.god.common.constant.AuthConstant;

public final class TokenUtil {

    private TokenUtil() {
    }

    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (authorizationHeader.startsWith(AuthConstant.TOKEN_PREFIX)) {
            return authorizationHeader.substring(AuthConstant.TOKEN_PREFIX.length());
        }
        return authorizationHeader;
    }

    public static String buildBearerToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return AuthConstant.TOKEN_PREFIX + token;
    }
}
