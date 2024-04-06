package com.coding.fullstack.ware.service.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.ware.dao.PurchaseDetailDao;
import com.coding.fullstack.ware.entity.PurchaseDetailEntity;
import com.coding.fullstack.ware.service.PurchaseDetailService;

@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity>
    implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String)params.get("key");
        String status = (String)params.get("status");
        String wareId = (String)params.get("wareId");

        LambdaQueryWrapper<PurchaseDetailEntity> lambdaQuery = Wrappers.lambdaQuery(PurchaseDetailEntity.class);

        if (StringUtils.isNotEmpty(key)) {
            lambdaQuery
                .and(i -> i.eq(PurchaseDetailEntity::getPurchaseId, key).or().eq(PurchaseDetailEntity::getSkuId, key));
        }
        if (StringUtils.isNotEmpty(status)) {
            lambdaQuery.eq(PurchaseDetailEntity::getStatus, status);
        }
        if (StringUtils.isNotEmpty(wareId)) {
            lambdaQuery.eq(PurchaseDetailEntity::getWareId, wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), lambdaQuery);

        return new PageUtils(page);
    }

}