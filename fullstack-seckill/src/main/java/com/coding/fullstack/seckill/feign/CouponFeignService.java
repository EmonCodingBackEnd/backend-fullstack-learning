package com.coding.fullstack.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.coding.common.utils.R;

@FeignClient("fullstack-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();
}
