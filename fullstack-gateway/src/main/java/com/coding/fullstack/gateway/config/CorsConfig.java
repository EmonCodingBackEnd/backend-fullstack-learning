package com.coding.fullstack.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/** 网关开放跨域 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // config.addAllowedOrigin("*"); // 跨域没有cookie时可用
        config.setAllowCredentials(true); // 允许跨域cookie
        /*
         * When allowCredentials is true, allowedOrigins cannot contain the special value "*" since that cannot be set
         * on the "Access-Control-Allow-Origin" response header. To allow credentials to a set of origins, list them
         * explicitly or consider using "allowedOriginPatterns" instead.
         */
        config.addAllowedOriginPattern("*"); // 跨域带有cookie时可用
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L); // 在有效时间内，浏览器无须为同一请求再次发起预检请求。请注意，浏览器自身维护了一个最大有效时间，如果该字段值超过了最大有效时间则不生效！

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
