package com.coding.fullstack.auth.controller;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.coding.common.constant.AuthConstant;
import com.coding.common.utils.R;
import com.coding.common.vo.MemberEntityVo;
import com.coding.fullstack.auth.feign.MemberFeignService;
import com.coding.fullstack.auth.util.HttpUtils;
import com.coding.fullstack.auth.vo.SocialGiteeUser;
import com.coding.fullstack.auth.vo.SocialGitterBasicInfo;
import com.coding.fullstack.auth.vo.SocialGitterEmailInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理社交登录请求
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class OAuth2Controller {

    private final MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/gitee/callback")
    public String gitee(@RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "error", required = false) String error,
        @RequestParam(value = "error_description", required = false) String errorDescription,
        RedirectAttributes attributes, HttpSession session, HttpServletResponse oriResponse) throws Exception {
        log.info("进入gitee回调 code={} error={} error_description={}", code, error, errorDescription);
        if (code == null && error != null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", errorDescription);
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.fsmall.com/login.html";
        }

        // 1、根据code兑换accessToken
        String host = "https://gitee.com";
        String path = "/oauth/token";
        Map<String, String> headers = new HashMap<>();
        Map<String, String> querys = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        querys.put("grant_type", "authorization_code");
        querys.put("code", code);
        querys.put("client_id", "af59a1f46c54ae472128d69f78da231cb95028bea81490063715f8ef947139d7");
        querys.put("redirect_uri", "http://auth.fsmall.com/oauth2.0/gitee/callback");
        querys.put("client_secret", "855fc40f37e4826ed4687bdfd8e986af8cfb3c7d8cbf73a5a8a75cbf32a78cd1");
        HttpResponse response = HttpUtils.doPost(host, path, "POST", headers, querys, bodys);
        log.info("response:{}", response);

        int statusCode = response.getStatusLine().getStatusCode();
        // 2、根据accessToken获取用户信息
        if (statusCode == 200) {
            /*
            {
                "access_token": "117da3da8585de33ea9a485dcaec7b30",
                "token_type": "bearer",
                "expires_in": 86400,
                "refresh_token": "60052475019591f964c6f09415eb28603c2e54340f46133432e38aba288cb6d5",
                "scope": "user_info emails",
                "created_at": 1713243742
            }
             */
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("result:{}", result);
            SocialGiteeUser socialGiteeUser = JSON.parseObject(result, SocialGiteeUser.class);
            log.info("socialGiteeUser:{}", socialGiteeUser);

            // 首次登录时，为当前用户自动注册
            String emailInfo = getEmailInfo(socialGiteeUser.getAccessToken());
            log.info("emailInfo:{}", emailInfo);
            List<SocialGitterEmailInfo> socialGitterEmailInfos =
                JSON.parseObject(emailInfo, new TypeReference<List<SocialGitterEmailInfo>>() {});
            socialGiteeUser.setEmailInfos(socialGitterEmailInfos);
            String basicInfo = getBasicInfo(socialGiteeUser.getAccessToken());
            socialGiteeUser.setSocialGitterBasicInfo(JSON.parseObject(basicInfo, SocialGitterBasicInfo.class));
            log.info("basicInfo:{}", basicInfo);
            R login = memberFeignService.login(socialGiteeUser);
            if (login.getCode() == 0) {
                MemberEntityVo data = login.getData(new TypeReference<MemberEntityVo>() {});
                log.info("登录成功，用户信息：{}", data);
                // 1、第一次使用session，命令浏览器保存卡号。JSESSIONID这个cookie
                // 2、以后浏览器访问哪个网站，就会带上这个网站的cookie
                // 子域之间：fsmall.com auth.fsmall.com search.fsmall.com
                // 发卡时（指定父域名），即使是子域系统发的卡，也能让父域直接访问

                // TODO: 2024/4/18 1、默认发的令牌。session=dkxiskdk。作用域：当前域 auth.fsmall.com（解决子域sessioin共享问题）
                // TODO: 2024/4/18 2、使用JSON的序列化方式来序列化对象数据到redis中
                session.setAttribute(AuthConstant.LOGIN_USER, data);
                return "redirect:http://fsmall.com";
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("msg", (String)login.get("msg"));
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.fsmall.com/login.html";
            }
        } else {
            // {"error":"invalid_grant","error_description":"授权方式无效，或者登录回调地址无效、过期或已被撤销"}
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", result);
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.fsmall.com/login.html";
        }
    }

    private String getEmailInfo(String accessToken) throws Exception {
        String host = "https://gitee.com";
        String path = "/api/v5/emails";
        Map<String, String> headers = new HashMap<>();
        Map<String, String> querys = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        querys.put("access_token", accessToken);
        HttpResponse response = HttpUtils.doGet(host, path, "GET", headers, querys);
        log.info("emailInfoResponse:{}", response);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("获取用户邮箱信息失败");
        }
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }

    private String getBasicInfo(String accessToken) throws Exception {
        String host = "https://gitee.com";
        String path = "/api/v5/user";
        Map<String, String> headers = new HashMap<>();
        Map<String, String> querys = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        querys.put("access_token", accessToken);
        HttpResponse response = HttpUtils.doGet(host, path, "GET", headers, querys);
        log.info("basicInfoResponse:{}", response);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("获取用户信息失败");
        }
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }
}
