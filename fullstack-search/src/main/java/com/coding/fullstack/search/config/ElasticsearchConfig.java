package com.coding.fullstack.search.config;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.profiles.active:dev}")
    private String profile;

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        // builder.addHeader("Authorization", "Bearer " + TOKEN);
        // builder.setHttpAsyncResponseConsumerFactory(
        // new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient() {
        String host = "192.168.32.116";
        Integer port = 31533;
        if ("prod".equals(profile)) {
            host = "es-nodeport.fsmall-project";
            port = 9200;
        }
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
        return client;
    }

    @Bean
    public ElasticsearchClient esClient() {
        // URL and API key
        String serverUrl = "http://192.168.32.116:31533";
        if ("prod".equals(profile)) {
            serverUrl = "http://es-nodeport.fsmall-project:9200";
        }
        String apiKey = "VnVhQ2ZHY0JDZGJrU...";

        // Create the low-level client
        RestClient restClient = RestClient.builder(HttpHost.create(serverUrl))
            .setDefaultHeaders(new Header[] {new BasicHeader("Authorization", "ApiKey " + apiKey)}).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient esClient = new ElasticsearchClient(transport);
        return esClient;
    }
}
