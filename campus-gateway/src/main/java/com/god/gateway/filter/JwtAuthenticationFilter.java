package com.god.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.god.common.constant.AuthConstant;
import com.god.common.result.ResultCode;
import com.god.common.utils.JwtUtil;
import com.god.common.utils.TokenUtil;
import com.god.gateway.config.SecurityProperties;
import com.god.gateway.util.GatewayResponseUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final SecurityProperties securityProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(AuthConstant.AUTHORIZATION_HEADER);
        String token = TokenUtil.extractToken(authorization);
        if (StrUtil.isBlank(token)) {
            return GatewayResponseUtil.writeError(exchange.getResponse(), HttpStatus.UNAUTHORIZED, ResultCode.UNAUTHORIZED);
        }

        try {
            Claims claims = JwtUtil.parseToken(token, securityProperties.getJwtSecret());
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(AuthConstant.USER_ID_HEADER, getClaimAsString(claims, "userId"))
                    .header(AuthConstant.USERNAME_HEADER, getClaimAsString(claims, "username"))
                    .header(AuthConstant.USER_ROLE_HEADER, getClaimAsString(claims, "role"))
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception ex) {
            return GatewayResponseUtil.writeError(exchange.getResponse(), HttpStatus.UNAUTHORIZED, ResultCode.TOKEN_INVALID);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitePath(String path) {
        List<String> whiteList = securityProperties.getWhiteList();
        if (CollUtil.isEmpty(whiteList)) {
            return false;
        }
        return whiteList.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private String getClaimAsString(Claims claims, String key) {
        Object value = claims.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
