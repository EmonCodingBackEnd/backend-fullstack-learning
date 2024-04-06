package com.coding.fullstack.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.coding.fullstack.coupon.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class FullstackCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackCouponApplication.class, args);
    }

}
