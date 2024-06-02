package com.coding.fullstack.seckill.config;

import java.io.IOException;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
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
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://repo.emon.vip:6379").setPassword("redis123").setDatabase(0)
            .setTimeout(3000);
        return Redisson.create(config);
    }
}
