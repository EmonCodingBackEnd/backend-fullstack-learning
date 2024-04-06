package com.coding.fullstack.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.coding.fullstack.order.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class FullstackOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackOrderApplication.class, args);
    }

}
