package com.coding.fullstack.product.vo;

import java.util.List;

import lombok.Data;

@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SkuItemBaseAttrVo> attrs;
}
