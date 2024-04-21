package com.coding.fullstack.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.coding.fullstack.product.dao")
@EnableFeignClients(basePackages = "com.coding.fullstack.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class FullstackProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackProductApplication.class, args);
    }

}
