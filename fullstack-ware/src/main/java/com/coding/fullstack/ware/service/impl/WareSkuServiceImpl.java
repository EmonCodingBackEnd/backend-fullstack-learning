package com.coding.fullstack.ware.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.exception.NoStockException;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.utils.R;
import com.coding.fullstack.ware.dao.WareSkuDao;
import com.coding.fullstack.ware.entity.WareSkuEntity;
import com.coding.fullstack.ware.feign.ProductFeignService;
import com.coding.fullstack.ware.service.WareSkuService;
import com.coding.fullstack.ware.vo.OrderItemVo;
import com.coding.fullstack.ware.vo.SkuHasStockVo;
import com.coding.fullstack.ware.vo.WareSkuLockVo;

import lombok.Data;
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

    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 1、按照下单的收货地址，找到一个就近仓库，锁定库存
        // 2、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> stocks = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪里有库存
            List<Long> wareIds = this.baseMapper.listWareIdHasStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 2、锁定库存
        for (SkuWareHasStock stock : stocks) {
            boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // 成功就返回1，否则返回0
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    break;
                    // 锁定成功
                    // LockStockResult result = new LockStockResult();
                    // result.setSkuId(skuId);
                    // result.setNum(stock.getNum());
                    // result.setWareId(wareId);
                    // result.setLocked(true);
                    // return result;
                } else {
                    // 当前仓库锁失败，重试下一个仓库
                }
            }

            // 当前商品的所有仓库都没有锁住
            if (!skuStocked) {
                throw new NoStockException(skuId);
            }
        }

        // 3、肯定全部都是锁定成功的
        return true;
    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }
}