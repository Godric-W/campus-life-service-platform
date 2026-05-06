package com.god.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.god.notification.mapper")
@SpringBootApplication(scanBasePackages = {"com.god.notification", "com.god.common"})
public class CampusNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusNotificationApplication.class, args);
    }
}
