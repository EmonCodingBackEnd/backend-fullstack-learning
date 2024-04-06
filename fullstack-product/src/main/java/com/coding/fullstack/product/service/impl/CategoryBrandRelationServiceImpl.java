package com.coding.fullstack.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.exception.RRException;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.BrandDao;
import com.coding.fullstack.product.dao.CategoryBrandRelationDao;
import com.coding.fullstack.product.dao.CategoryDao;
import com.coding.fullstack.product.entity.BrandEntity;
import com.coding.fullstack.product.entity.CategoryBrandRelationEntity;
import com.coding.fullstack.product.entity.CategoryEntity;
import com.coding.fullstack.product.service.CategoryBrandRelationService;

import lombok.RequiredArgsConstructor;

@Service("categoryBrandRelationService")
@RequiredArgsConstructor
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity>
    implements CategoryBrandRelationService {

    private final BrandDao brandDao;
    private final CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(new Query<CategoryBrandRelationEntity>().getPage(params),
            Wrappers.lambdaQuery(CategoryBrandRelationEntity.class));

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity category = categoryDao.selectById(catelogId);
        if (brandEntity == null || category == null) {
            throw new RRException("品牌或分类不存在");
        }

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(category.getName());

        boolean result = this.save(categoryBrandRelation);
        if (!result) {
            throw new RRException("保存失败");
        }
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        LambdaUpdateWrapper<CategoryBrandRelationEntity> lambdaUpdateWrapper = Wrappers
            .lambdaUpdate(CategoryBrandRelationEntity.class).set(CategoryBrandRelationEntity::getBrandName, name)
            .eq(CategoryBrandRelationEntity::getBrandId, brandId);
        this.update(lambdaUpdateWrapper);
    }

    @Override
    public void updateCategory(Long catId, String name) {
        LambdaUpdateWrapper<CategoryBrandRelationEntity> lambdaUpdateWrapper = Wrappers
            .lambdaUpdate(CategoryBrandRelationEntity.class).set(CategoryBrandRelationEntity::getCatelogName, name)
            .eq(CategoryBrandRelationEntity::getCatelogId, catId);
        this.update(lambdaUpdateWrapper);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities = this.baseMapper.selectList(Wrappers
            .lambdaQuery(CategoryBrandRelationEntity.class).eq(CategoryBrandRelationEntity::getCatelogId, catId));
        List<BrandEntity> brandEntities = categoryBrandRelationEntities.stream().map(item -> {
            Long brandId = item.getBrandId();
            BrandEntity brand = brandDao.selectById(brandId);
            return brand;
        }).collect(Collectors.toList());
        return brandEntities;
    }
}