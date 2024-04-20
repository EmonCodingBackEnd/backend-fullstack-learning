package com.coding.fullstack.auth.feign;

import com.coding.fullstack.auth.vo.SocialGiteeUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.coding.common.utils.R;
import com.coding.fullstack.auth.vo.UserLoginVo;
import com.coding.fullstack.auth.vo.UserRegistVo;

@FeignClient("fullstack-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo registVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo loginVo);

    @PostMapping("/member/member/socialLogin")
    R login(@RequestBody SocialGiteeUser loginVo);
}
