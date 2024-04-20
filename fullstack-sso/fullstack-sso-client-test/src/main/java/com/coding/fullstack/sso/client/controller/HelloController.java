package com.coding.fullstack.sso.client.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HelloController {

    @Value("${sso.server.url.login}")
    private String ssoServerLoginUrl;

    @Value("${sso.server.url.info}")
    private String ssoServerUserInfoUrl;

    /**
     * 无需登录就可访问
     * 
     * @return
     */
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    /**
     * 需登录就可访问
     *
     * @return
     */
    @GetMapping("/employees")
    public String employees(Model model, HttpSession session, HttpServletRequest request,
        @RequestParam(value = "token", required = false) String token) {
        if (!ObjectUtils.isEmpty(token)) {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity =
                restTemplate.getForEntity(ssoServerUserInfoUrl + "?" + "token=" + token, String.class);
            String username = forEntity.getBody();
            session.setAttribute("loginUser", username);
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            String redirectUrl =
                String.format("redirect:%s?redirect_uri=%s", ssoServerLoginUrl, request.getRequestURL());
            log.info("未登录，跳转到 {}", redirectUrl);
            return redirectUrl;
        }
        List<String> emps = new ArrayList<>();
        emps.add("张三");
        emps.add("李四");
        model.addAttribute("emps", emps);
        return "list";
    }
}
