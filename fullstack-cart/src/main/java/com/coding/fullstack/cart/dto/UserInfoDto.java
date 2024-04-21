package com.coding.fullstack.cart.dto;

import lombok.Data;

@Data
public class UserInfoDto {
    private Long userId;
    private String userKey;

    // 请求中是否存在临时用户
    private boolean tempUser = false;
}
