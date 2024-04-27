package com.coding.fullstack.order.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.coding.fullstack.order.vo.OrderItemVo;

@FeignClient("fullstack-cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> currentUserCartItems();
}
