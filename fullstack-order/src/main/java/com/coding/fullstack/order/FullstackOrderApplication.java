package com.coding.fullstack.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.coding.fullstack.order.dao")
@EnableFeignClients(basePackages = "com.coding.fullstack.order.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class FullstackOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackOrderApplication.class, args);
    }

}
