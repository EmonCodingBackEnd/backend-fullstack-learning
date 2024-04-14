package com.coding.fullstack.auth.vo;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class UserLoginVo {
    @NotEmpty(message = "用户名不能为空")
    private String loginacct;
    @NotEmpty(message = "密码不能为空")
    private String password;
}
