package com.coding.fullstack.coupon.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.to.MemberPrice;
import com.coding.common.to.SkuReductionTo;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.coupon.dao.SkuFullReductionDao;
import com.coding.fullstack.coupon.entity.MemberPriceEntity;
import com.coding.fullstack.coupon.entity.SkuFullReductionEntity;
import com.coding.fullstack.coupon.entity.SkuLadderEntity;
import com.coding.fullstack.coupon.service.MemberPriceService;
import com.coding.fullstack.coupon.service.SkuFullReductionService;
import com.coding.fullstack.coupon.service.SkuLadderService;

@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity>
    implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;
    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page =
            this.page(new Query<SkuFullReductionEntity>().getPage(params), new QueryWrapper<SkuFullReductionEntity>());

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 6.4、Sku的优惠信息和满减信息
        // 6.4.1、sms_sku_ladder 阶梯价格
        if (skuReductionTo.getFullCount() > 0) {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
            skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
            skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
            skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
            skuLadderService.save(skuLadderEntity);
        }

        // 6.4.2、sms_sku_full_reduction 满减价格
        if (skuReductionTo.getFullPrice() != null && skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
            SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
            skuFullReductionEntity.setSkuId(skuReductionTo.getSkuId());
            skuFullReductionEntity.setFullPrice(skuReductionTo.getFullPrice());
            skuFullReductionEntity.setReducePrice(skuReductionTo.getReducePrice());
            skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
            this.save(skuFullReductionEntity);
        }

        // 6.4.3、sms_member_price 会员价格
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceList = memberPrices.stream()
            .filter(item -> item.getPrice() != null && item.getPrice().compareTo(BigDecimal.ZERO) > 0).map(item -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setMemberLevelName(item.getName());
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setAddOther(1);
                return memberPriceEntity;
            }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceList);
    }
}