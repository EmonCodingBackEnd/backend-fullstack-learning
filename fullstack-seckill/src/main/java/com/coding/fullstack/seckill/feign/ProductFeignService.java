package com.coding.fullstack.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.coding.common.utils.R;

@FeignClient("fullstack-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    // @RequiresPermissions("product:skuinfo:info")
    R info(@PathVariable("skuId") Long skuId);
}
