package com.coding.fullstack.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.coding.common.utils.R;

@FeignClient("fullstack-thirdparty")
public interface ThirdpartyFeignService {

    @GetMapping("/thirdparty/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
