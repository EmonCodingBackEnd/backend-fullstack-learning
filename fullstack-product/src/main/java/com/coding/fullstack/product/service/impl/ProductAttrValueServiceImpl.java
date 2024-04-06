package com.coding.fullstack.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.ProductAttrValueDao;
import com.coding.fullstack.product.entity.AttrEntity;
import com.coding.fullstack.product.entity.ProductAttrValueEntity;
import com.coding.fullstack.product.service.AttrService;
import com.coding.fullstack.product.service.ProductAttrValueService;
import com.coding.fullstack.product.vo.spu.BaseAttrs;

import lombok.RequiredArgsConstructor;

@Service("productAttrValueService")
@RequiredArgsConstructor
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity>
    implements ProductAttrValueService {

    private final AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page =
            this.page(new Query<ProductAttrValueEntity>().getPage(params), new QueryWrapper<ProductAttrValueEntity>());

        return new PageUtils(page);
    }

    @Override
    public void saveSpuBaseAttrValues(Long id, List<BaseAttrs> baseAttrs) {
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(id);
            productAttrValueEntity.setAttrId(item.getAttrId());
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());

        this.saveBatch(productAttrValueEntities);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        List<ProductAttrValueEntity> productAttrValueEntities = this.baseMapper
            .selectList(Wrappers.lambdaQuery(ProductAttrValueEntity.class).eq(ProductAttrValueEntity::getSpuId, spuId));
        return productAttrValueEntities;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> pavs) {
        // 1、删除这个spuId之前对应的所有属性
        this.baseMapper
            .delete(Wrappers.lambdaQuery(ProductAttrValueEntity.class).eq(ProductAttrValueEntity::getSpuId, spuId));
        // 新增
        pavs.forEach(item -> {
            item.setSpuId(spuId);
            this.save(item);
        });
    }
}