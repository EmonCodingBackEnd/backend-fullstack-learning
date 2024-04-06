package com.coding.fullstack.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.product.entity.AttrEntity;
import com.coding.fullstack.product.vo.AttrGroupRelationVo;
import com.coding.fullstack.product.vo.AttrResponseVo;
import com.coding.fullstack.product.vo.AttrVo;

/**
 * 商品属性
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseOrSaleAttrPage(final Integer attrType, Map<String, Object> params, Long catelogId);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    /**
     * 获取当前分组关联的所有属性
     * 
     * @param attrgroupId
     * @return
     */
    List<AttrEntity> getRelationAttr(Long attrgroupId);

    /**
     * 获取当前分组未关联的属性
     * 
     * @param params
     * @param attrgroupId
     * @return
     */
    PageUtils getRelationNoAttr(Map<String, Object> params, Long attrgroupId);

    boolean deleteRelation(List<AttrGroupRelationVo> attrGroups);

    /**
     * 根据attrId查询其中的可检索属性
     * 
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrs(List<Long> attrIds);
}
