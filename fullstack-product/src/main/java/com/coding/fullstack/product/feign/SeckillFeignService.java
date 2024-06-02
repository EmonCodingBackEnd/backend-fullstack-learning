package com.coding.fullstack.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coding.common.utils.R;

@FeignClient("fullstack-seckill")
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getCurrentSeckillInfo(@PathVariable("skuId") Long skuId);
}
