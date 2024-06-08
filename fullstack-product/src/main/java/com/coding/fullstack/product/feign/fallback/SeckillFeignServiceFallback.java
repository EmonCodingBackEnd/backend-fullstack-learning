package com.coding.fullstack.product.feign.fallback;

import org.springframework.stereotype.Component;

import com.coding.common.exception.BizCodeEnum;
import com.coding.common.utils.R;
import com.coding.fullstack.product.feign.SeckillFeignService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {
    @Override
    public R getCurrentSeckillInfo(Long skuId) {
        log.warn("熔断方法调用...getCurrentSeckillInfo({})", skuId);
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
    }
}
