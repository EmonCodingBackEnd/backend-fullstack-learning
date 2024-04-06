/**
 * Copyright 2024 bejson.com
 */
package com.coding.fullstack.product.vo.spu;

import java.math.BigDecimal;
import java.util.List;

import com.coding.common.to.MemberPrice;

import lombok.Data;

@Data
public class Skus {

    private List<Attr> attr;
    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;
    private List<Images> images;
    private List<String> descar;
    private int fullCount; // 满几件
    private BigDecimal discount; // 打几折
    private int countStatus; // 是否叠加其他优惠[0-不可叠加，1-可叠加]
    private BigDecimal fullPrice; // 满多少
    private BigDecimal reducePrice; // 减多少
    private int priceStatus; // 是否参与其他优惠
    private List<MemberPrice> memberPrice;

}