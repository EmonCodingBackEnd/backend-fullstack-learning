package com.coding.fullstack.search;

import java.io.IOException;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson2.JSON;
import com.coding.fullstack.search.config.ElasticsearchConfig;
import com.coding.fullstack.search.vo.BankVo;
import com.coding.fullstack.search.vo.User;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

@SpringBootTest
class FullstackSearchApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    ElasticsearchClient elasticsearchClient;

    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }

    /**
     * 测试存储数据到es
     * 
     * 保存或更新
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("1");

        User user = new User();
        user.setUserName("kimchy");
        user.setGender("male");
        user.setAge(18);
        String userJsonString = JSON.toJSONString(user);
        request.source(userJsonString, XContentType.JSON);

        IndexResponse indexResponse = restHighLevelClient.index(request, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(indexResponse);
    }

    @Test
    public void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // 指定检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        // 按照年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);

        // 计算平均年龄
        AvgAggregationBuilder ageAvg = AggregationBuilders.avg("ageAvg").field("age");
        searchSourceBuilder.aggregation(ageAvg);

        // 计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        // 不显示搜索数据
        // searchSourceBuilder.size(0);

        System.out.println(searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);

        // 执行检索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);

        // 分析结果
        /*
        {
          "took" : 23,
          "timed_out" : false,
          "_shards" : {
            "total" : 1,
            "successful" : 1,
            "skipped" : 0,
            "failed" : 0
          },
          "hits" : {
            "total" : {
              "value" : 4,
              "relation" : "eq"
            },
            "max_score" : null,
            "hits" : [ ]
          },
          "aggregations" : {
            "ageAgg" : {
              "doc_count_error_upper_bound" : 0,
              "sum_other_doc_count" : 0,
              "buckets" : [
                {
                  "key" : 38,
                  "doc_count" : 2
                },
                {
                  "key" : 28,
                  "doc_count" : 1
                },
                {
                  "key" : 32,
                  "doc_count" : 1
                }
              ]
            },
            "ageAvg" : {
              "value" : 34.0
            },
            "balanceAvg" : {
              "value" : 25208.0
            }
          }
        }
         */
        System.out.println(searchResponse);
        RestStatus status = searchResponse.status();
        TimeValue took = searchResponse.getTook();
        Boolean terminatedEarly = searchResponse.isTerminatedEarly();
        boolean timedOut = searchResponse.isTimedOut();

        int totalShards = searchResponse.getTotalShards();
        int successfulShards = searchResponse.getSuccessfulShards();
        int failedShards = searchResponse.getFailedShards();
        for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
            // failures should be handled here
        }

        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();
        // the total number of hits, must be interpreted in the context of totalHits.relation
        long numHits = totalHits.value;
        // whether the number of hits is accurate (EQUAL_TO) or a lower bound of the total (GREATER_THAN_OR_EQUAL_TO)
        TotalHits.Relation relation = totalHits.relation;
        float maxScore = hits.getMaxScore();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            // do something with the SearchHit
            String index = hit.getIndex();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            BankVo bankVo = JSON.parseObject(sourceAsString, BankVo.class);
            System.out.println("bankVo = " + bankVo);
        }

        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAggAggregation = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAggAggregation.getBuckets()) {
            Number keyAsNumber = bucket.getKeyAsNumber();
            long docCount = bucket.getDocCount();
            System.out.println("keyAsNumber = " + keyAsNumber + ", docCount = " + docCount);
        }
        Aggregation ageAvgAggregation = aggregations.get("ageAvg");
        System.out.println("ageAvgAggregation = " + ((ParsedAvg) ageAvgAggregation).getValue());
        Aggregation balanceAvgAggregation = aggregations.get("balanceAvg");
        System.out.println("balanceAvgAggregation = " + ((ParsedAvg) balanceAvgAggregation).getValue());
    }

}
