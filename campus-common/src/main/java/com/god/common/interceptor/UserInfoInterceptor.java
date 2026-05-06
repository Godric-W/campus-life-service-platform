package com.god.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.god.common.constant.AuthConstant;
import com.god.common.context.UserContext;
import com.god.common.dto.LoginUserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(AuthConstant.USER_ID_HEADER);
        if (StrUtil.isBlank(userId)) {
            return true;
        }

        LoginUserDTO loginUser = LoginUserDTO.builder()
                .userId(parseLong(userId))
                .username(request.getHeader(AuthConstant.USERNAME_HEADER))
                .role(request.getHeader(AuthConstant.USER_ROLE_HEADER))
                .build();
        UserContext.setUser(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
