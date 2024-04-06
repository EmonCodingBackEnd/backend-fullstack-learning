package com.coding.fullstack.search.vo;

import java.math.BigDecimal;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.Data;

@Data
public class BankVo {
    @JSONField(name = "account_number")
    private Integer accountNumber;
    private BigDecimal balance;
    private String firstname;
    private String lastname;
    private Integer age;
    private String gender;
    private String address;
    private String employer;
    private String email;
    private String city;
    private String state;
}
