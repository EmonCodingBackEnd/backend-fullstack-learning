package com.coding.fullstack.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

// @formatter:off
/**
 * 1、整合Sentinel
 *  1）、导入依赖 spring-cloud-starter-alibaba-sentinel
 *  2）、下载sentinel的控制台
 *  3）、配置sentinel控制台地址信息
 *  4）、在控制台调整参数。【默认所有的流控设置保存在内存里，重启失效！】
 *
 * 2、每一个微服务都导入 spring-boot-starter-actuator【1.8版本sentinel版本并不需要】
 *
 * 3、自定义sentinel流控返回数据
 *
 * 4、使用sentinel来保护feign远程调用：熔断与降级，假设A->B服务
 *  1）、调用方的熔断保护：feign.sentinel.enabled=true（A服务）
 *  2）、调用方手动指定远程服务的熔断策略：（A服务，提供fallback）
 *  3）、超大流量时，必须牺牲一些远程服务。在服务的提供方（远程服务）指定降级策略；（B服务）
 *      提供方是在运行中的，但不执行业务逻辑，而是直接返回降级应答。（也即是BlockExceptionHandler的应答）
 *
 * 5、自定义受保护的资源
 *  1）、基于代码：
 *      try (Entry entry = SphU.entry("seckillSkus")) {} catch (BlockException e) {}
 *  2）、基于注解
 *      @SentinelResource(value = "getCurrentSeckillSkus", blockHandler = "blockHandler", fallback = "fallback")
 */
// @formatter:on

@MapperScan("com.coding.fullstack.seckill.dao")
@EnableFeignClients(basePackages = "com.coding.fullstack.seckill.feign")
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FullstackSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackSeckillApplication.class, args);
    }

}
