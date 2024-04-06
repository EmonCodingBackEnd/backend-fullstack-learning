/**
 * Copyright 2024 bejson.com
 */
package com.coding.fullstack.product.vo.spu;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Bounds {

    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}