package com.coding.fullstack.thirdparty.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coding.common.utils.R;
import com.coding.fullstack.thirdparty.component.SmsComponent;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SmsSendController {
    private final SmsComponent smsComponent;

    /**
     * 提供给别的服务进行调用
     * 
     * @param phone
     * @param code
     * @return
     */

    @GetMapping("/thirdparty/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        return smsComponent.sendSmsCode(phone, code);
    }
}
