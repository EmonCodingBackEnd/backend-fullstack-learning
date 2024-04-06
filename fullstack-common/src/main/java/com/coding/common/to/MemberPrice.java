/**
 * Copyright 2024 bejson.com
 */
package com.coding.common.to;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;

}