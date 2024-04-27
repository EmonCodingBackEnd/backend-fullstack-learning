package com.coding.fullstack.order.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                // 拿到请求头中的cookie
                HttpServletRequest request = requestAttributes.getRequest();
                String cookie = request.getHeader("Cookie");
                // 设置到请求头中
                template.header("Cookie", cookie);
            }
        };
    }
}
