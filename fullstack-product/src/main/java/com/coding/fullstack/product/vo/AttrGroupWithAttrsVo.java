package com.coding.fullstack.product.vo;

import java.util.List;

import com.coding.fullstack.product.entity.AttrEntity;

import lombok.Data;

@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 分组下的属性列表
     */
    private List<AttrEntity> attrs;
}
