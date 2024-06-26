package com.coding.fullstack.search.vo;

import java.util.ArrayList;
import java.util.List;

import com.coding.common.to.es.SkuEsModel;

import lombok.Data;

@Data
public class SearchResult {
    // 查询到的所有商品信息
    private List<SkuEsModel> products;
    /**
     * 分页信息
     */
    private Integer pageNum; // 当前页码
    private Long total; // 总记录数
    private Integer totalPages; // 总页码
    private List<Integer> pageNavs;// 导航页

    private List<BrandVo> brands; // 当前查询到的结果涉及到的品牌
    private List<CatalogVo> catalogs; // 当前查询到的结果涉及到的分类
    private List<AttrVo> attrs; // 当前查询到的结果涉及到的属性

    // 增加面包屑导航的返回数据
    private List<NavVo> navs;
    private List<Long> attrIds = new ArrayList<>();

    // ==========以上是返回给页面的数据==========

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }
}
