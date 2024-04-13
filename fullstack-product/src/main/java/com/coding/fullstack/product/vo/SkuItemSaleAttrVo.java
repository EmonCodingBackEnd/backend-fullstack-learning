package com.coding.fullstack.product.vo;

import java.util.List;

import lombok.Data;

@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
