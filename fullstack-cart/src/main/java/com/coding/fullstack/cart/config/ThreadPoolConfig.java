package com.coding.fullstack.cart.config;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool) {
        return new ThreadPoolExecutor(pool.getCoreSize(), pool.getMaxSize(), pool.getKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(pool.getQueueSize()), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());
    }
}
