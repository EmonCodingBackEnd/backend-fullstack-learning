package com.coding.fullstack.ware.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.utils.R;
import com.coding.fullstack.ware.dao.WareSkuDao;
import com.coding.fullstack.ware.entity.WareSkuEntity;
import com.coding.fullstack.ware.feign.ProductFeignService;
import com.coding.fullstack.ware.service.WareSkuService;
import com.coding.fullstack.ware.vo.SkuHasStockVo;

import lombok.RequiredArgsConstructor;

@Service("wareSkuService")
@RequiredArgsConstructor
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    private final ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String skuId = (String)params.get("skuId");
        String wareId = (String)params.get("wareId");

        LambdaQueryWrapper<WareSkuEntity> lambdaQuery = Wrappers.lambdaQuery(WareSkuEntity.class);
        if (skuId != null && !skuId.isEmpty()) {
            lambdaQuery.eq(WareSkuEntity::getSkuId, skuId);
        }
        if (wareId != null && !wareId.isEmpty()) {
            lambdaQuery.eq(WareSkuEntity::getWareId, wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), lambdaQuery);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断如果还没有库存记录，就新增
        WareSkuEntity wareSkuEntity = this.baseMapper.selectOne(Wrappers.lambdaQuery(WareSkuEntity.class)
            .eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId));
        if (wareSkuEntity != null) {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        } else {
            WareSkuEntity wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setStock(skuNum);
            wareSku.setWareId(wareId);
            wareSku.setStockLocked(0);
            // 远程查询并设置sku的名字，如果失败，整个事务无需回滚！
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>)info.get("skuInfo");
                    wareSku.setSkuName((String)skuInfo.get("skuName"));
                }
            } catch (Exception e) {
                log.error("远程查询并设置sku的名字异常", e);
            }
            this.baseMapper.insert(wareSku);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            Long count = this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count != null && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }
}