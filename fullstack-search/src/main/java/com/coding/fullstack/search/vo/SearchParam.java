package com.coding.fullstack.search.vo;

import java.util.List;

import lombok.Data;

/**
 * 封装页面所有可能传递过来的查询条件
 *
 * catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=1&skuPrice=1_500&brandId=1&brandId=2&attrs=1_其他&attrs=2_5寸:6寸
 */
@Data
public class SearchParam {
    /**
     * 全文检索
     */
    private String keyword; // 页面传递过来的检索关键字
    // @formatter:off
    /**
     * 过滤
     *
     * hasStock skuPrice brandId catalog3Id attrs
     * hasStock=1/0
     * skuPrice=1_500/_500/500_
     * brandId=1&brandId=2
     *
     */
    // @formatter:on
    private List<String> attrs; // 按照属性进行筛选，可以多选
    private Long catalog3Id; // 3级分类id
    private List<Long> brandId; // 按照品牌进行查询，可以多选
    private String skuPrice; // 价格区间查询
    private Integer hasStock = 1; // 是否只显示有货
    /**
     * 排序
     *
     * sort=saleCount_asc sort=skuPrice_asc sort=hotScore_asc
     */
    private String sort; // 排序条件
    /**
     * 分页
     */
    private Integer pageNum = 1; // 从1开始

    /*
     * 聚合品牌、分类、属性
     */
}
