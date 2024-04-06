package com.coding.fullstack.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.AttrAttrgroupRelationDao;
import com.coding.fullstack.product.entity.AttrAttrgroupRelationEntity;
import com.coding.fullstack.product.service.AttrAttrgroupRelationService;
import com.coding.fullstack.product.vo.AttrGroupRelationVo;

@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity>
    implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page =
            this.page(new Query<AttrAttrgroupRelationEntity>().getPage(params), new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Override
    public boolean saveBatchAttrRelation(List<AttrGroupRelationVo> attrGroupRelationVos) {
        if (CollectionUtils.isEmpty(attrGroupRelationVos)) {
            return false;
        }

        List<AttrAttrgroupRelationEntity> relations = attrGroupRelationVos.stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(item.getAttrId());
            relationEntity.setAttrGroupId(item.getAttrGroupId());
            return relationEntity;
        }).collect(Collectors.toList());
        return this.saveBatch(relations);
    }
}