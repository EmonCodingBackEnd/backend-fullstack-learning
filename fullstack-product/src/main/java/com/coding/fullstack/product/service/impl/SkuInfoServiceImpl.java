package com.coding.fullstack.product.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.SkuInfoDao;
import com.coding.fullstack.product.entity.SkuInfoEntity;
import com.coding.fullstack.product.service.SkuInfoService;

import lombok.RequiredArgsConstructor;

@Service("skuInfoService")
@RequiredArgsConstructor
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page =
            this.page(new Query<SkuInfoEntity>().getPage(params), new QueryWrapper<SkuInfoEntity>());

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfos(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String)params.get("key");
        String min = (String)params.get("min");
        String max = (String)params.get("max");
        String brandId = (String)params.get("brandId");
        String catelogId = (String)params.get("catelogId");

        LambdaQueryWrapper<SkuInfoEntity> lambdaQuery = Wrappers.lambdaQuery(SkuInfoEntity.class);
        if (StringUtils.isNotEmpty(catelogId) && !"0".equals(catelogId)) {
            lambdaQuery.eq(SkuInfoEntity::getCatalogId, catelogId);
        }
        if (StringUtils.isNotEmpty(brandId) && !"0".equals(brandId)) {
            lambdaQuery.eq(SkuInfoEntity::getBrandId, brandId);
        }
        if (StringUtils.isNotEmpty(min)) {
            lambdaQuery.ge(SkuInfoEntity::getPrice, min);
        }
        if (StringUtils.isNotEmpty(max) && !"0".equals(max)) {
            lambdaQuery.le(SkuInfoEntity::getPrice, max);
        }
        if (StringUtils.isNotEmpty(key)) {
            lambdaQuery.and(i -> i.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key));
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), lambdaQuery);

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(Wrappers.lambdaQuery(SkuInfoEntity.class).eq(SkuInfoEntity::getSpuId, spuId));
    }
}