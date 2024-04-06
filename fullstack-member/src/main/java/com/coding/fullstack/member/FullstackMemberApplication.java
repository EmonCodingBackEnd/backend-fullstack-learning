package com.coding.fullstack.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.coding.fullstack.member.dao")
@EnableFeignClients(basePackages = "com.coding.fullstack.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class FullstackMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackMemberApplication.class, args);
    }

}
