package com.coding.fullstack.search.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.coding.common.to.es.SkuEsModel;
import com.coding.fullstack.search.config.ElasticsearchConfig;
import com.coding.fullstack.search.constant.EsConstant;
import com.coding.fullstack.search.service.ProductSaveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSaveServiceImpl implements ProductSaveService {

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        if (CollectionUtils.isEmpty(skuEsModels)) {
            return true;
        }
        // 保存到ES
        // 1、给es中建立索引。product
        // 2、给es中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String json = JSON.toJSONString(skuEsModel);
            indexRequest.source(json, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, ElasticsearchConfig.COMMON_OPTIONS);

        // 如果批量错误，这里需要执行错误处理
        boolean b = bulkResponse.hasFailures();
        if (b) {
            List<String> collect =
                    Arrays.stream(bulkResponse.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
            log.error("商品上架错误：{}", collect);
        }

        return !b;
    }
}
