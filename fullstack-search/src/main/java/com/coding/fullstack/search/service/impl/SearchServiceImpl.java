package com.coding.fullstack.search.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.coding.common.to.es.SkuEsModel;
import com.coding.common.utils.R;
import com.coding.fullstack.search.config.ElasticsearchConfig;
import com.coding.fullstack.search.constant.EsConstant;
import com.coding.fullstack.search.feign.ProductFeignService;
import com.coding.fullstack.search.service.SearchService;
import com.coding.fullstack.search.vo.AttrResponseVo;
import com.coding.fullstack.search.vo.BrandEntity;
import com.coding.fullstack.search.vo.SearchParam;
import com.coding.fullstack.search.vo.SearchResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final RestHighLevelClient restHighLevelClient;
    private final ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        // 1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            // 2、执行检索请求
            SearchResponse searchResponse =
                restHighLevelClient.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
            // 3、封装响应数据
            searchResult = buildSearchResult(searchParam, searchResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return searchResult;
    }

    // 准备检索请求

    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        // 指定检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /*
        查询：模糊匹配，过滤（按照属性，分类，品牌，库存，价格区间）
         */
        // 1、构建bool-query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1、must - 模糊匹配
        if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 1.2、filter - 三级分类id过滤
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // 1.2、filter - 品牌id过滤
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        // 1.2、filter - 属性
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            /*
            attrs=1_其他&attrs=2_5寸:6寸
             */
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] split = StringUtils.split(attr, "_");
                String attrId = split[0];
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                String[] attrValues = StringUtils.split(split[1], ":");
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                // 每一个属性的条件，都需要生成一个嵌入的布尔查询条件
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }
        // 1.2、filter - 库存
        if (searchParam.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }
        // 1.2、filter - 价格区间
        if (StringUtils.isNotEmpty(searchParam.getSkuPrice())) {
            /*
            {
                "range": {
                    "skuPrice": {
                        "gte": 5000,
                        "lte": 10000
                    }
                }
            }
             */
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String skuPrice = StringUtils.trimToEmpty(searchParam.getSkuPrice());
            String[] split = skuPrice.split("_");
            if (split.length == 2) {
                // 避免 skuPrice= _10000 这种情况，需要trimToNull
                rangeQueryBuilder.gte(StringUtils.trimToNull(split[0])).lte(split[1]);
            } else if (split.length == 1) {
                if (skuPrice.startsWith("_")) {
                    rangeQueryBuilder.lte(split[0]);
                } else if (skuPrice.endsWith("_")) {
                    rangeQueryBuilder.gte(split[0]);
                }
            }

            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);

        /*
        排序、分页、高亮
         */
        // 2.1、排序
        if (StringUtils.isNotEmpty(searchParam.getSort())) {
            /*
            sort=saleCount_asc
             */
            String sort = searchParam.getSort();
            String[] split = StringUtils.split(sort, "_");
            String field = split[0];
            String order = split[1];
            searchSourceBuilder.sort(field, "asc".equalsIgnoreCase(order) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 2.2、分页
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 2.3、高亮
        if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /*
        聚合：品牌，分类，属性
         */
        // 3.1、按照品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brandAgg);

        // 3.2、按照分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalogAgg);

        // 3.3、按照属性聚合
        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        nested.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(nested);

        log.info("DSL={}", searchSourceBuilder);
        return new SearchRequest(new String[] {EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }

    // 封装响应数据
    private SearchResult buildSearchResult(SearchParam searchParam, SearchResponse searchResponse) {
        SearchResult searchResult = new SearchResult();
        // 1、返回的所有查询到的商品
        SearchHits hits = searchResponse.getHits();
        List<SkuEsModel> esModelList = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                // 高亮
                if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    esModel.setSkuTitle(skuTitle.getFragments()[0].string());
                }
                esModelList.add(esModel);
            }
        }
        searchResult.setProducts(esModelList);

        // 2、分页信息-页码和记录数
        long total = hits.getTotalHits().value;
        searchResult.setPageNum(searchParam.getPageNum());
        searchResult.setTotal(total);
        searchResult.setTotalPages((int)Math.ceil((double)total / EsConstant.PRODUCT_PAGESIZE));
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= searchResult.getTotalPages(); i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        // 够级属性的面包屑导航数据
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = searchParam.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] splits = attr.split("_");
                navVo.setNavValue(splits[1]);
                searchResult.getAttrIds().add(Long.parseLong(splits[0]));
                R info = productFeignService.info(Long.parseLong(splits[0]));
                if (info.getCode() == 0) {
                    AttrResponseVo attrVo = info.getData("attr", new TypeReference<AttrResponseVo>() {});
                    navVo.setNavName(attrVo.getAttrName());
                } else {
                    navVo.setNavName(splits[0]);
                }
                // 取消了这个面包屑以后，我们要跳转到哪个地方，将请求地址的url里面的当前置空
                String replace = replaceQueryString(searchParam.getQueryString(), attr, "attrs");
                navVo.setLink("http://search.fsmall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVos);
        }

        // 构建品牌和分类的面包屑导航
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = searchResult.getNavs();
            if (navs == null) {
                navs = new ArrayList<>();
            }

            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");

            R info = productFeignService.infos(searchParam.getBrandId());
            if (info.getCode() == 0) {
                List<BrandEntity> brands = info.getData("brands", new TypeReference<List<BrandEntity>>() {});
                StringBuilder buffer = new StringBuilder();
                String replace = searchParam.getQueryString();
                for (BrandEntity brand : brands) {
                    buffer.append(brand.getName()).append(";");
                    // 取消了这个面包屑以后，我们要跳转到哪个地方，将请求地址的url里面的当前置空
                    replace = replaceQueryString(replace, brand.getBrandId().toString(), "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.fsmall.com/list.html?" + replace);
                navs.add(navVo);
            }

        }

        // 3、当前所有商品涉及到的所有品牌信息、分类信息、属性信息
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        List<? extends Terms.Bucket> brandBuckets = brandAgg.getBuckets();
        brandBuckets.forEach(bucket -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        });
        searchResult.setBrands(brandVos);

        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> catalogBuckets = catalogAgg.getBuckets();
        catalogBuckets.forEach(bucket -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        });
        searchResult.setCatalogs(catalogVos);

        ParsedNested attrAgg = aggregations.get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        List<? extends Terms.Bucket> attrIdBuckets = attrIdAgg.getBuckets();
        for (Terms.Bucket bucket : attrIdBuckets) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream()
                .map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        searchResult.setAttrs(attrVos);

        return searchResult;
    }

    private String replaceQueryString(String queryString, String value, String key) {
        String encodeValue;
        try {
            encodeValue = URLEncoder.encode(value, "UTF-8");
            encodeValue = encodeValue.replace("+", "%20");
            encodeValue = encodeValue.replace("%3B", ";");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return queryString.replace("&" + key + "=" + encodeValue, "");
    }
}
