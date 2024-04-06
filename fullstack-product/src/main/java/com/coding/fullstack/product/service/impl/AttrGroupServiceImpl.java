package com.coding.fullstack.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.AttrGroupDao;
import com.coding.fullstack.product.entity.AttrEntity;
import com.coding.fullstack.product.entity.AttrGroupEntity;
import com.coding.fullstack.product.service.AttrGroupService;
import com.coding.fullstack.product.service.AttrService;
import com.coding.fullstack.product.vo.AttrGroupWithAttrsVo;

import lombok.RequiredArgsConstructor;

@Service("attrGroupService")
@RequiredArgsConstructor
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    private final AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page =
            this.page(new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<AttrGroupEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page;
        String key = StringUtils.trimToEmpty((String)params.get("key"));

        LambdaQueryWrapper<AttrGroupEntity> lambda = new QueryWrapper<AttrGroupEntity>().lambda();
        lambda.eq(catelogId != 0, AttrGroupEntity::getCatelogId, catelogId);
        lambda.and(StringUtils.isNotEmpty(key),
            c -> c.eq(AttrGroupEntity::getAttrGroupId, key).or().likeRight(AttrGroupEntity::getAttrGroupName, key));
        page = this.page(new Query<AttrGroupEntity>().getPage(params), lambda);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 1、查出当前分类下的所有属性分组
        List<AttrGroupEntity> attrGroupWithAttrsVos =
            this.list(Wrappers.lambdaUpdate(AttrGroupEntity.class).eq(AttrGroupEntity::getCatelogId, catelogId));
        //
        // 2、查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos = attrGroupWithAttrsVos.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> relationAttr = attrService.getRelationAttr(item.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(relationAttr);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return vos;
    }
}