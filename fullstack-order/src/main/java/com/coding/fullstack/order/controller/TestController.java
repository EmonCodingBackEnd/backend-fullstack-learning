package com.coding.fullstack.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/living")
    public String living(String info, HttpServletRequest request) {
        log.info("living=>{}", info);
        return "received";
    }
}
