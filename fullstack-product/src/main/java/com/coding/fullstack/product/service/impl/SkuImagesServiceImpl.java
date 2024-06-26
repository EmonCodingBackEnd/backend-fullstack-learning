package com.coding.fullstack.product.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.SkuImagesDao;
import com.coding.fullstack.product.entity.SkuImagesEntity;
import com.coding.fullstack.product.service.SkuImagesService;

@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page =
            this.page(new Query<SkuImagesEntity>().getPage(params), new QueryWrapper<SkuImagesEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<SkuImagesEntity> getImagesBySkuId(Long skuId) {
        return this.list(Wrappers.lambdaQuery(SkuImagesEntity.class).eq(SkuImagesEntity::getSkuId, skuId));
    }
}