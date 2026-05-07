package com.god.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.god.file", "com.god.common"})
@EnableDiscoveryClient
@ConfigurationPropertiesScan
public class CampusFileApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusFileApplication.class, args);
    }
}
