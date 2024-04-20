package com.coding.fullstack.ssolserver.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final StringRedisTemplate stringRedisTemplate;

    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("token") String token) {
        return stringRedisTemplate.opsForValue().get(token);
    }

    @GetMapping("/login.html")
    public String login(@RequestParam("redirect_uri") String redirectUri, Model model,
        @CookieValue(value = "sso_token", required = false) String ssoToken) {
        if (!ObjectUtils.isEmpty(ssoToken)) {
            String username = stringRedisTemplate.opsForValue().get(ssoToken);
            if (!ObjectUtils.isEmpty(username)) {
                // 登录成功跳转，调回到之前的页面
                return "redirect:" + redirectUri + "?token=" + ssoToken;
            }
        }
        model.addAttribute("redirectUri", redirectUri);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(String username, String password, @RequestParam("redirect_uri") String redirectUri,
        Model model, HttpServletResponse oriResponse) {
        if (!ObjectUtils.isEmpty(username) && !ObjectUtils.isEmpty(username)) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);
            Cookie cookie = new Cookie("sso_token", uuid);
            oriResponse.addCookie(cookie);
            // 登录成功跳转，调回到之前的页面
            return "redirect:" + redirectUri + "?token=" + uuid;
        }
        model.addAttribute("redirectUri", redirectUri);
        return "login";
    }

    /**
     * 需登录就可访问
     *
     * @return
     */
    @GetMapping("/employees")
    public String employees(Model model, HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        List<String> emps = new ArrayList<>();
        emps.add("张三");
        emps.add("李四");
        model.addAttribute("emps", emps);
        return "list";
    }
}
