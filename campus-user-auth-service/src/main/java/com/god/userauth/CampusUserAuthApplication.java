package com.god.userauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@ConfigurationPropertiesScan
@MapperScan("com.god.userauth.mapper")
@SpringBootApplication(scanBasePackages = {"com.god.userauth", "com.god.common"})
public class CampusUserAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusUserAuthApplication.class, args);
    }
}
