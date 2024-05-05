package com.coding.fullstack.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 本地事务失效问题：
 * 同一个对象内事务方法互调默认失效，原因：绕过了代理对象，事务使用代理对象来控制的
 * 解决：使用代理对象来调用事务方法
 * 1、引入aop-starter: spring-boot-starter-aop
 * 2、启用 @EnableAspectJAutoProxy(exposeProxy = true) // 开启aspectj动态代理，开启后动态代理都是aspectj创建的（即使没有接口也可以创建动态代理）
 * 3、使用代理对象来调用事务方法
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.coding.fullstack.order.dao")
@EnableFeignClients(basePackages = "com.coding.fullstack.order.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class FullstackOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackOrderApplication.class, args);
    }

}
