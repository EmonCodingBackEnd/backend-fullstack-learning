package com.coding.fullstack.coupon.service.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.coupon.dao.SeckillSkuRelationDao;
import com.coding.fullstack.coupon.entity.SeckillSkuRelationEntity;
import com.coding.fullstack.coupon.service.SeckillSkuRelationService;

@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity>
    implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String promotionSessionId = (String)params.get("promotionSessionId");

        LambdaQueryWrapper<SeckillSkuRelationEntity> lambdaQuery = Wrappers.lambdaQuery(SeckillSkuRelationEntity.class);
        lambdaQuery.eq(StringUtils.isNotEmpty(promotionSessionId), SeckillSkuRelationEntity::getPromotionSessionId,
            promotionSessionId);

        IPage<SeckillSkuRelationEntity> page =
            this.page(new Query<SeckillSkuRelationEntity>().getPage(params), lambdaQuery);

        return new PageUtils(page);
    }

}