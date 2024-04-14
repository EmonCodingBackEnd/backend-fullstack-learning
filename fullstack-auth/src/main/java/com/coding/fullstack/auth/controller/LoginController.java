package com.coding.fullstack.auth.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coding.common.constant.AuthConstant;
import com.coding.common.exception.BizCodeEnum;
import com.coding.common.utils.R;
import com.coding.fullstack.auth.feign.MemberFeignService;
import com.coding.fullstack.auth.feign.ThirdpartyFeignService;
import com.coding.fullstack.auth.vo.UserLoginVo;
import com.coding.fullstack.auth.vo.UserRegistVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final ThirdpartyFeignService thirdpartyFeignService;
    private final MemberFeignService memberFeignService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发送一个请求直接跳转到一个页面
     * <p>
     * SpringMVC viewController：将请求和页面映射过来。
     *
     * @return
     */

    /*@GetMapping("/login.html")
    public String login() {
        return "login";
    }
    
    @GetMapping("/reg.html")
    public String reg() {
        return "reg";
    }*/
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        String key = AuthConstant.SMS_CODE_CACHE_PREFIX + phone;
        String oldValue = stringRedisTemplate.opsForValue().get(key);
        if (oldValue != null) {
            long oldTime = Long.parseLong(oldValue.split("_")[1]);
            long cha = System.currentTimeMillis() - oldTime;
            if (cha < 60 * 1000) {
                return R.error(BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getCode(),
                    BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = RandomStringUtils.randomNumeric(6);
        String value = String.format("%s_%s", code, System.currentTimeMillis());
        stringRedisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
        log.info("验证码是：{}", code);
        // return thirdpartyFeignService.sendCode(phone, code);
        return R.ok();
    }

    // RedirectAttributes attributes：模拟重定向携带数据，利用session原理。
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo registVo, BindingResult bindingResult, Model model,
        RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors()
                .forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
            // model.addAttribute("errors", errors);
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.fsmall.com/reg.html";
        }
        // 真正注册。调用远程服务注册
        // 1、校验验证码
        String phone = registVo.getPhone();
        String code = registVo.getCode();
        String key = AuthConstant.SMS_CODE_CACHE_PREFIX + phone;
        String oldValue = stringRedisTemplate.opsForValue().get(key);
        if (oldValue != null) {
            String[] s = oldValue.split("_");
            // 验证码通过
            if (code.equals(s[0])) {
                // 删除验证码
                stringRedisTemplate.delete(key);
                // 发送真正的注册请求，远程
                R regist = memberFeignService.regist(registVo);
                if (regist.getCode() == 0) {
                    // 注册成功回到首页，回到登录页
                    return "redirect:/http://auth.fsmall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", (String)regist.get("msg"));
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.fsmall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.fsmall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.fsmall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(@Valid UserLoginVo loginVo, BindingResult bindingResult,
                        RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors()
                .forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.fsmall.com/login.html";
        }

        R login = memberFeignService.login(loginVo);
        if (login.getCode() == 0) {
            // TODO: 2024/4/14 登录成功后的处理
            // attributes.addFlashAttribute("user", login.getData(MemEnt))
            return "redirect:http://fsmall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", (String)login.get("msg"));
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.fsmall.com/login.html";
        }
    }
}
