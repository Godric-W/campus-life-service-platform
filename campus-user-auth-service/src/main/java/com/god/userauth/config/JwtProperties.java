package com.god.userauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "campus.security")
public class JwtProperties {

    private String jwtSecret;
    private Long tokenExpireSeconds = 86400L;
}
