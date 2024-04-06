package com.coding.fullstack.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.coding.fullstack.ware.dao")
@EnableFeignClients(basePackages = "com.coding.fullstack.ware.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class FullstackWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackWareApplication.class, args);
    }

}
