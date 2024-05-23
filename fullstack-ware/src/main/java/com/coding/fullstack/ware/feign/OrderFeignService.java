package com.coding.fullstack.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coding.common.utils.R;

@FeignClient("fullstack-order")
public interface OrderFeignService {

    @GetMapping("/order/order/status/{orderSn}")
    R getOrderByOrderSn(@PathVariable("orderSn") String orderSn);
}
