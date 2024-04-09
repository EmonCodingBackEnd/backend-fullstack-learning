package com.coding.fullstack.product.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.constant.ProductConstant;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.AttrAttrgroupRelationDao;
import com.coding.fullstack.product.dao.AttrDao;
import com.coding.fullstack.product.dao.AttrGroupDao;
import com.coding.fullstack.product.dao.CategoryDao;
import com.coding.fullstack.product.entity.AttrAttrgroupRelationEntity;
import com.coding.fullstack.product.entity.AttrEntity;
import com.coding.fullstack.product.entity.AttrGroupEntity;
import com.coding.fullstack.product.entity.CategoryEntity;
import com.coding.fullstack.product.service.AttrService;
import com.coding.fullstack.product.service.CategoryService;
import com.coding.fullstack.product.vo.AttrGroupRelationVo;
import com.coding.fullstack.product.vo.AttrResponseVo;
import com.coding.fullstack.product.vo.AttrVo;

import lombok.RequiredArgsConstructor;

@Service("attrService")
@RequiredArgsConstructor
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    private final AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    private final AttrGroupDao attrGroupDao;
    private final CategoryDao categoryDao;
    private final CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), new QueryWrapper<AttrEntity>());

        return new PageUtils(page);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        // 如果是规格参数（基本属性），才需要处理属性分组信息
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attr.getAttrType()) {
            if (attr.getAttrGroupId() != null) {
                AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                relationEntity.setAttrId(attrEntity.getAttrId());
                relationEntity.setAttrGroupId(attr.getAttrGroupId());
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    @Override
    public PageUtils queryBaseOrSaleAttrPage(final Integer attrType, Map<String, Object> params, Long catelogId) {
        String key = StringUtils.trimToEmpty((String)params.get("key"));

        LambdaQueryWrapper<AttrEntity> lambdaQuery = Wrappers.lambdaQuery(AttrEntity.class);
        lambdaQuery.eq(AttrEntity::getAttrType, attrType);
        lambdaQuery.eq(catelogId != 0, AttrEntity::getCatelogId, catelogId);
        lambdaQuery.and(StringUtils.isNotEmpty(key),
            c -> c.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key));

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), lambdaQuery);
        List<AttrResponseVo> records = page.getRecords().stream().map(attr -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attr, attrResponseVo);

            // TODO: 2024/3/17 当前一个属性对应一个分组
            // 如果是规格参数（基本属性），才需要处理属性分组信息
            if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attrType) {
                AttrAttrgroupRelationEntity relationEntity =
                    attrAttrgroupRelationDao.selectOne(Wrappers.lambdaQuery(AttrAttrgroupRelationEntity.class)
                        .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId())
                        .select(AttrAttrgroupRelationEntity::getAttrGroupId));
                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    attrResponseVo.setAttrGroupId(relationEntity.getAttrGroupId());
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    if (attrGroupEntity != null) {
                        attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }

            CategoryEntity category = categoryDao.selectById(attr.getCatelogId());
            if (category != null) {
                attrResponseVo.setCatelogName(category.getName());
            }
            return attrResponseVo;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(records);
        return pageUtils;
    }

    @Cacheable(value = "attr", key = "'attrinfo:'+#attrId")
    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        if (attrEntity != null) {
            BeanUtils.copyProperties(attrEntity, attrResponseVo);

            // TODO: 2024/3/17 当前一个属性对应一个分组
            // 如果是规格参数（基本属性），才需要处理属性分组信息
            if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attrEntity.getAttrType()) {
                AttrAttrgroupRelationEntity relationEntity =
                    attrAttrgroupRelationDao.selectOne(Wrappers.lambdaQuery(AttrAttrgroupRelationEntity.class)
                        .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId())
                        .select(AttrAttrgroupRelationEntity::getAttrGroupId));
                if (relationEntity != null) {
                    attrResponseVo.setAttrGroupId(relationEntity.getAttrGroupId());
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    if (attrGroupEntity != null) {
                        attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }

            CategoryEntity category = categoryDao.selectById(attrEntity.getCatelogId());
            if (category != null) {
                attrResponseVo.setCatelogName(category.getName());
                Long catelogId = attrEntity.getCatelogId();
                Long[] catelogPath = categoryService.findCatelogPath(catelogId);
                attrResponseVo.setCatelogPath(catelogPath);
            }
        }

        return attrResponseVo;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        // TODO: 2024/3/17 当前一个属性对应一个分组
        // 如果是规格参数（基本属性），才需要处理属性分组信息
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attr.getAttrType()) {
            if (attr.getAttrGroupId() != null) {
                AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                relationEntity.setAttrId(attrEntity.getAttrId());
                relationEntity.setAttrGroupId(attr.getAttrGroupId());

                LambdaQueryWrapper<AttrAttrgroupRelationEntity> lambdaQueryWrapper =
                    Wrappers.lambdaQuery(AttrAttrgroupRelationEntity.class).eq(AttrAttrgroupRelationEntity::getAttrId,
                        attrEntity.getAttrId());
                Long count = attrAttrgroupRelationDao.selectCount(lambdaQueryWrapper);
                if (count > 0) {
                    attrAttrgroupRelationDao.update(relationEntity, lambdaQueryWrapper);
                } else {
                    attrAttrgroupRelationDao.insert(relationEntity);
                }
            }
        }
    }

    /**
     * 根据分组id查找关联的所有基本属性
     *
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities =
            attrAttrgroupRelationDao.selectList(Wrappers.lambdaQuery(AttrAttrgroupRelationEntity.class)
                .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrgroupId));

        List<Long> attrIds = entities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        List<AttrEntity> attrEntities = new ArrayList<>();
        if (attrIds.size() == 0) {
            return attrEntities;
        }
        attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    @Override
    public PageUtils getRelationNoAttr(Map<String, Object> params, Long attrgroupId) {
        String key = StringUtils.trimToEmpty((String)params.get("key"));

        // 当前分组只能关联自己所属分类里面的属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 当前分组只能关联其他分组没有关联到的属性
        // 当前分类下的其他分组信息
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao
            .selectList(Wrappers.lambdaQuery(AttrGroupEntity.class).eq(AttrGroupEntity::getCatelogId, catelogId));
        List<Long> attrGroupIdList =
            attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        // 这些分组已经关联到的属性
        List<Long> attrIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(attrGroupIdList)) {
            List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities =
                attrAttrgroupRelationDao.selectList(Wrappers.lambdaQuery(AttrAttrgroupRelationEntity.class)
                    .in(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupIdList));
            attrIdList = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());
        }
        // 排除这些已经被同分类下其他分组使用到的属性
        LambdaQueryWrapper<AttrEntity> attrEntityLambdaQueryWrapper = Wrappers.lambdaQuery(AttrEntity.class);
        attrEntityLambdaQueryWrapper.eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        attrEntityLambdaQueryWrapper.and(StringUtils.isNotEmpty(key),
            c -> c.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key));
        attrEntityLambdaQueryWrapper.eq(AttrEntity::getCatelogId, catelogId)
            .notIn(CollectionUtils.isNotEmpty(attrIdList), AttrEntity::getAttrId, attrIdList);
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), attrEntityLambdaQueryWrapper);
        return new PageUtils(page);
    }

    @Override
    public boolean deleteRelation(List<AttrGroupRelationVo> attrGroups) {
        if (CollectionUtils.isEmpty(attrGroups)) {
            return true;
        }
        LambdaQueryWrapper<AttrAttrgroupRelationEntity> lambdaQueryWrapper =
            Wrappers.lambdaQuery(AttrAttrgroupRelationEntity.class);
        lambdaQueryWrapper.in(AttrAttrgroupRelationEntity::getAttrId,
            attrGroups.stream().map(AttrGroupRelationVo::getAttrId).collect(Collectors.toList()));
        lambdaQueryWrapper.in(AttrAttrgroupRelationEntity::getAttrGroupId,
            attrGroups.stream().map(AttrGroupRelationVo::getAttrGroupId).collect(Collectors.toList()));
        attrAttrgroupRelationDao.delete(lambdaQueryWrapper);
        return true;
    }

    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {
        if (CollectionUtils.isEmpty(attrIds)) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectSearchAttrs(attrIds);
    }
}