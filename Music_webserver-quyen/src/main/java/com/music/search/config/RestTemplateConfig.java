package com.music.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);

        // QUAN TRỌNG: tắt auto-decompress để tránh ký tự lạ ????
        // Spring Boot 3 không cần dòng nào nữa – chỉ cần dùng factory này là đủ!
        return new RestTemplate(factory);
    }
}