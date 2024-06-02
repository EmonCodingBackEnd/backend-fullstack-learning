package com.coding.fullstack.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.coding.fullstack.seckill.dao")
@EnableFeignClients(basePackages = "com.coding.fullstack.seckill.feign")
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FullstackSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackSeckillApplication.class, args);
    }

}
