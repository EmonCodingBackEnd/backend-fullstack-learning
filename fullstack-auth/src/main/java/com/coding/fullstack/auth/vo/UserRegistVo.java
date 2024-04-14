package com.coding.fullstack.auth.vo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class UserRegistVo {
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 6, max = 18, message = "用户名长度必须为6-18位")
    private String username;
    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 18, message = "密码长度必须为6-18位")
    private String password;
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码不能为空")
    private String code;
}
