package com.coding.fullstack.product.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.utils.R;
import com.coding.fullstack.product.dao.SkuInfoDao;
import com.coding.fullstack.product.entity.SkuImagesEntity;
import com.coding.fullstack.product.entity.SkuInfoEntity;
import com.coding.fullstack.product.entity.SpuInfoDescEntity;
import com.coding.fullstack.product.feign.SeckillFeignService;
import com.coding.fullstack.product.service.*;
import com.coding.fullstack.product.vo.SeckillInfoVo;
import com.coding.fullstack.product.vo.SkuItemSaleAttrVo;
import com.coding.fullstack.product.vo.SkuItemVo;
import com.coding.fullstack.product.vo.SpuItemAttrGroupVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("skuInfoService")
@RequiredArgsConstructor
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    private final SkuImagesService skuImagesService;
    private final SpuInfoDescService spuInfoDescService;
    private final AttrGroupService attrGroupService;
    private final SkuSaleAttrValueService skuSaleAttrValueService;
    private final ThreadPoolExecutor executor;
    private final SeckillFeignService seckillFeignService;

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

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        // 1、sku基本信息获取 pms_sku_info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            boolean hasStock = true;
            skuItemVo.setHasStock(hasStock);
            return info;
        }, executor);

        // 2、sku的图片信息
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        // 3、spu的销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(info -> {
            Long spuId = info.getSpuId();
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
            skuItemVo.setSaleAttrs(saleAttrs);
        }, executor);

        // 4、spu的描述信息
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(info -> {
            Long spuId = info.getSpuId();
            SpuInfoDescEntity desc = spuInfoDescService.getById(spuId);
            skuItemVo.setDesc(desc);
        }, executor);

        // 5、spu的规格参数信息
        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync(info -> {
            Long spuId = info.getSpuId();
            Long catalogId = info.getCatalogId();
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
            skuItemVo.setAttrGroupVos(attrGroupVos);
        }, executor);

        // 6、当前商品是否参与了秒杀优惠
        CompletableFuture<Void> seckillFuture = infoFuture.thenAcceptAsync(info -> {
            log.info("根据 skuId={} 查询秒杀信息！", skuId);
            R seckillInfo = seckillFeignService.getCurrentSeckillInfo(skuId);
            if (seckillInfo.getCode() == 0) {
                SeckillInfoVo seckillInfoVo = seckillInfo.getData(new TypeReference<SeckillInfoVo>() {});
                skuItemVo.setSeckillInfo(seckillInfoVo);
            }
        }, executor);

        CompletableFuture<Void> allOf =
            CompletableFuture.allOf(saleAttrFuture, descFuture, attrGroupFuture, imageFuture, seckillFuture);
        allOf.get();

        return skuItemVo;
    }

    @Override
    public BigDecimal getPrice(Long skuId) {
        SkuInfoEntity byId = this.getById(skuId);
        return byId != null ? byId.getPrice() : null;
    }
}