package com.coding.fullstack.product.config;

import java.io.IOException;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    /**
     * 所有对Redisson的使用都是通过RedissonClient对象
     * 
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") String port,
        @Value("${spring.redis.password}") String password) throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s", host, port)).setPassword(password)
            .setDatabase(0).setTimeout(3000);
        return Redisson.create(config);
    }
}
