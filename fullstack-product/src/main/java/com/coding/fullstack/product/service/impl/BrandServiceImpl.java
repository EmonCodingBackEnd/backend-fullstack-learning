package com.coding.fullstack.product.service.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.BrandDao;
import com.coding.fullstack.product.entity.BrandEntity;
import com.coding.fullstack.product.service.BrandService;
import com.coding.fullstack.product.service.CategoryBrandRelationService;

import lombok.RequiredArgsConstructor;

@Service("brandService")
@RequiredArgsConstructor
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    private final CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String)params.get("key");
        LambdaQueryWrapper<BrandEntity> lambda = new QueryWrapper<BrandEntity>().lambda();
        if (StringUtils.isNotEmpty(key)) {
            lambda.eq(BrandEntity::getBrandId, key);
            lambda.or().likeLeft(BrandEntity::getName, key);
        }

        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), lambda);

        return new PageUtils(page);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public boolean updateDetail(BrandEntity brand) {
        boolean result = this.updateById(brand);
        if (!result) {
            return false;
        }

        if (StringUtils.isNotEmpty(brand.getName())) {
            // 同步更新其他关联表中的数据，允许更新品牌分类关系表记录数为0
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
            // TODO: 2024/3/16 更新其他关联
        }
        return true;
    }
}