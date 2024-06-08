package com.coding.fullstack.coupon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.fastjson2.JSON;
import com.coding.common.exception.BizCodeEnum;
import com.coding.common.utils.R;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SentinelConfig {
    @Bean
    public BlockExceptionHandler blockExceptionHandler() {
        return (request, response, e) -> {
            log.warn("降级方法调用...{}", request.getRequestURI());
            R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
            response.setCharacterEncoding("UTF-8");
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(JSON.toJSONString(error));
        };
    }
}
