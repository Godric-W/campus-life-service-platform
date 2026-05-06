package com.god.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "campus.security")
public class SecurityProperties {

    private String jwtSecret;
    private List<String> whiteList = new ArrayList<>();
}
