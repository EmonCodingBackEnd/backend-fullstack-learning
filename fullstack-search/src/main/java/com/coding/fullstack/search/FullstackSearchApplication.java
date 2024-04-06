package com.coding.fullstack.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// @MapperScan("com.coding.fullstack.product.dao")
// @EnableFeignClients(basePackages = "com.coding.fullstack.search.feign")
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FullstackSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackSearchApplication.class, args);
    }

}
