package com.onlineshop.order.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(RestClient.Builder builder,@Value("${product-service.api.base-url}") String baseUrl) {

        return builder
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .baseUrl(baseUrl)
                .build();
    }
}
