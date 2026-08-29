package cn.eta.team.eta.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient Bean
 * 
 * @author ormisnal
 * @since 2026-08-26
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // 设置连接超时和读超时
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "eta-backend")
                .build();
    }
}
