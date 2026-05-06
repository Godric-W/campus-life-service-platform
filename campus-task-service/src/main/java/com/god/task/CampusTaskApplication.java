package com.god.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.god.common.client")
@MapperScan("com.god.task.mapper")
@SpringBootApplication(scanBasePackages = {"com.god.task", "com.god.common"})
public class CampusTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusTaskApplication.class, args);
    }
}
