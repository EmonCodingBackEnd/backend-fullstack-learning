package com.coding.fullstack.product.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.SkuSaleAttrValueDao;
import com.coding.fullstack.product.entity.SkuSaleAttrValueEntity;
import com.coding.fullstack.product.service.SkuSaleAttrValueService;
import com.coding.fullstack.product.vo.SkuItemSaleAttrVo;

@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity>
    implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page =
            this.page(new Query<SkuSaleAttrValueEntity>().getPage(params), new QueryWrapper<SkuSaleAttrValueEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        List<SkuItemSaleAttrVo> saleAttrs = this.baseMapper.getSaleAttrsBySpuId(spuId);
        return saleAttrs;
    }
}