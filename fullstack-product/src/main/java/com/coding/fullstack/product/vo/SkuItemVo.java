package com.coding.fullstack.product.vo;

import java.util.List;

import com.coding.fullstack.product.entity.SkuImagesEntity;
import com.coding.fullstack.product.entity.SkuInfoEntity;
import com.coding.fullstack.product.entity.SpuInfoDescEntity;

import lombok.Data;

@Data
public class SkuItemVo {
    // 1、sku基本信息获取 pms_sku_info
    private SkuInfoEntity info;
    private boolean hasStock;
    // 2、sku的图片信息
    private List<SkuImagesEntity> images;
    // 3、spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttrs;
    // 4、spu的描述信息
    private SpuInfoDescEntity desc;
    // 5、spu的规格参数信息
    private List<SpuItemAttrGroupVo> attrGroupVos;
    // 6、当前商品的秒杀优惠信息
    private SeckillInfoVo seckillInfo;

}
