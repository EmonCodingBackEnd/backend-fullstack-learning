package com.coding.fullstack.member.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.coding.common.utils.R;

@FeignClient("fullstack-order")
public interface OrderFeignService {

    @RequestMapping("/order/order/listWithItem")
    // @RequiresPermissions("order:order:list")
    R listWithItem(@RequestBody Map<String, Object> params);
}
