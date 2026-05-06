package com.god.common.config;

import com.god.common.constant.AuthConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 传递用户信息 Header
            String userId = request.getHeader(AuthConstant.USER_ID_HEADER);
            if (userId != null) {
                template.header(AuthConstant.USER_ID_HEADER, userId);
            }
            
            String username = request.getHeader(AuthConstant.USERNAME_HEADER);
            if (username != null) {
                template.header(AuthConstant.USERNAME_HEADER, username);
            }
            
            String userRole = request.getHeader(AuthConstant.USER_ROLE_HEADER);
            if (userRole != null) {
                template.header(AuthConstant.USER_ROLE_HEADER, userRole);
            }
        }
    }
}
