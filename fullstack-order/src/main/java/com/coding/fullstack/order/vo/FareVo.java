package com.coding.fullstack.order.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
