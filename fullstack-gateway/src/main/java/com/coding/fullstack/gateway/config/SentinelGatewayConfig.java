package com.coding.fullstack.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.coding.common.exception.BizCodeEnum;
import com.coding.common.utils.R;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class SentinelGatewayConfig {
    public SentinelGatewayConfig() {
        GatewayCallbackManager.setBlockHandler((serverWebExchange, throwable) -> {
            log.warn("网关限流方法调用...{}", serverWebExchange.getRequest().getPath());
            R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
            return ServerResponse.ok().body(Mono.just(JSON.toJSONString(error)), String.class);
        });
    }
}
