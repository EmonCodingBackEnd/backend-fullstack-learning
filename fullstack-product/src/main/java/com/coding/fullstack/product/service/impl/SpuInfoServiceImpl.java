package com.coding.fullstack.product.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.constant.ProductConstant;
import com.coding.common.to.SkuReductionTo;
import com.coding.common.to.SpuBoundTo;
import com.coding.common.to.es.SkuEsModel;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.utils.R;
import com.coding.fullstack.product.dao.SpuInfoDao;
import com.coding.fullstack.product.entity.*;
import com.coding.fullstack.product.feign.CouponFeignService;
import com.coding.fullstack.product.feign.SearchFeignService;
import com.coding.fullstack.product.feign.WareFeignService;
import com.coding.fullstack.product.service.*;
import com.coding.fullstack.product.vo.SkuHasStockVo;
import com.coding.fullstack.product.vo.spu.*;

import lombok.RequiredArgsConstructor;

@Service("spuInfoService")
@RequiredArgsConstructor
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    private final SpuInfoDescService spuInfoDescService;
    private final SpuImagesService spuImagesService;
    private final ProductAttrValueService productAttrValueService;
    private final SkuInfoService skuInfoService;

    private final SkuImagesService skuImagesService;
    private final SkuSaleAttrValueService skuSaleAttrValueService;
    private final CouponFeignService couponFeignService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final AttrService attrService;
    private final WareFeignService wareFeignService;
    private final SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page =
            this.page(new Query<SpuInfoEntity>().getPage(params), new QueryWrapper<SpuInfoEntity>());

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1、保存SPU基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        // 3、保存Spu的图片集合 pms_spu_images
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        // 4、保存Spu的规格参数（基本参数）pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        productAttrValueService.saveSpuBaseAttrValues(spuInfoEntity.getId(), baseAttrs);

        // 5、、sms_spu_bounds 积分优惠
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败！");
        }

        // 6、保存Spu的Sku信息
        // 6.1、Sku的基本信息`pms_sku_info`
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus == null || skus.isEmpty()) {
            return;
        }

        skus.forEach(sku -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setSpuId(spuInfoEntity.getId());
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
            skuInfoEntity.setSaleCount(0L);
            for (Images image : sku.getImages()) {
                if (image.getDefaultImg() == 1) {
                    skuInfoEntity.setSkuDefaultImg(image.getImgUrl());
                }
            }
            skuInfoService.saveSkuInfos(skuInfoEntity);

            Long skuId = skuInfoEntity.getSkuId();

            // 6.2、Sku的图片信息`pms_sku_images`
            List<SkuImagesEntity> skuImagesEntities =
                sku.getImages().stream().filter(img -> StringUtils.isNotEmpty(img.getImgUrl())).map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntities);

            // 6.3、Sku的销售属性`pms_sku_sale_attr_value`
            List<Attr> attr = sku.getAttr();
            if (attr != null && attr.size() > 0) {
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    skuSaleAttrValueEntity.setAttrId(a.getAttrId());
                    skuSaleAttrValueEntity.setAttrName(a.getAttrName());
                    skuSaleAttrValueEntity.setAttrValue(a.getAttrValue());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
            }

            // 6.4、Sku的优惠信息和满减信息
            // 6.4.1、sms_sku_ladder 阶梯价格|sms_sku_full_reduction 满减价格|sms_member_price 会员价格
            SkuReductionTo skuReductionTo = new SkuReductionTo();
            BeanUtils.copyProperties(sku, skuReductionTo);
            skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
            if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                R r2 = couponFeignService.saveSkuReduction(skuReductionTo);
                if (r2.getCode() != 0) {
                    log.error("远程保存spu阶梯价格、满减价格、会员价格失败！");
                }
            }
        });

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String)params.get("key");
        String status = (String)params.get("status");
        String brandId = (String)params.get("brandId");
        String catelogId = (String)params.get("catelogId");

        LambdaQueryWrapper<SpuInfoEntity> lambdaQuery = Wrappers.lambdaQuery(SpuInfoEntity.class);
        if (StringUtils.isNotEmpty(catelogId) && !"0".equals(catelogId)) {
            lambdaQuery.eq(SpuInfoEntity::getCatalogId, catelogId);
        }
        if (StringUtils.isNotEmpty(brandId) && !"0".equals(brandId)) {
            lambdaQuery.eq(SpuInfoEntity::getBrandId, brandId);
        }
        if (StringUtils.isNotEmpty(status)) {
            lambdaQuery.eq(SpuInfoEntity::getPublishStatus, status);
        }
        if (StringUtils.isNotEmpty(key)) {
            lambdaQuery.and(i -> i.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key));
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), lambdaQuery);

        return new PageUtils(page);
    }

    @Override
    public boolean spuUp(Long spuId) {
        // 1、根据当前spuId获取所有sku信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 查询商品的所有可以被用来检索的基本属性（规格属性）
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        Set<Long> searchAttrIdSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrs =
            baseAttrs.stream().filter(item -> searchAttrIdSet.contains(item.getAttrId())).map(attrValue -> {
                SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
                BeanUtils.copyProperties(attrValue, attr);
                return attr;
            }).collect(Collectors.toList());

        Map<Long, Boolean> stockMap = null;
        try {
            R skusHasStock = wareFeignService.getSkusHasStock(skuIdList);
            if (skusHasStock.getCode() == 0) {
                stockMap = skusHasStock.getData(new TypeReference<List<SkuHasStockVo>>() {}).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            }
        } catch (Exception e) {
            log.error("远程查询sku是否有库存异常", e);
            return false;
        }

        // 2、封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> collect = skus.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            // 发送远程调用，查询是否有库存
            esModel.setHasStock(finalStockMap == null || finalStockMap.getOrDefault(sku.getSkuId(), false));
            // 热度评分 0
            esModel.setHotScore(0L);
            // 查询品牌名称和分类名称，以及品牌图片
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            if (brandEntity != null) {
                esModel.setBrandImg(brandEntity.getLogo());
                esModel.setBrandName(brandEntity.getName());
            }
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            if (categoryEntity != null) {
                esModel.setCatalogName(categoryEntity.getName());
            }
            // 设置可以被检索的基本属性（规格属性）
            esModel.setAttrs(attrs);

            return esModel;
        }).collect(Collectors.toList());

        // 将数据发送给es进行保存
        try {
            R productStatusUp = searchFeignService.productStatusUp(collect);
            if (productStatusUp.getCode() == 0) {
                // 远程调用成功
                // 修改当前spu的上架状态
                this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
            } else {
                // TODO: 2024/3/30 远程调用失败，重试！接口幂等性！
            }
        } catch (Exception e) {
            log.error("上架商品到es异常", e);
        }
        return true;
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        if (skuInfoEntity != null) {
            return this.getById(skuInfoEntity.getSpuId());
        }
        return null;
    }
}