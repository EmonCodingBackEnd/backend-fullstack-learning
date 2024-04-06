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
import com.coding.fullstack.ware.dao.WareInfoDao;
import com.coding.fullstack.ware.entity.WareInfoEntity;
import com.coding.fullstack.ware.service.WareInfoService;

@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String)params.get("key");

        LambdaQueryWrapper<WareInfoEntity> lambdaQuery = Wrappers.lambdaQuery(WareInfoEntity.class);

        if (StringUtils.isNotEmpty(key)) {
            lambdaQuery.and(i -> i.eq(WareInfoEntity::getId, key).or().like(WareInfoEntity::getName, key).or()
                .like(WareInfoEntity::getAddress, key));
        }

        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), lambdaQuery);

        return new PageUtils(page);
    }

}