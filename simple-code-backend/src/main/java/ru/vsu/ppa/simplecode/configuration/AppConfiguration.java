package ru.vsu.ppa.simplecode.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for the application.
 */
@Configuration
@RequiredArgsConstructor
public class AppConfiguration {

    private final JobeClientProperties jobeClientProperties;

    /**
     * Creates a RestClient bean for making HTTP requests to the Jobe API.
     *
     * @return the RestClient bean
     */
    @Bean
    public RestClient jobeRestClient() {
        return RestClient.builder()
                .baseUrl(jobeClientProperties.baseUrl())
                .requestFactory(customRequestFactory())
                .build();
    }

    /**
     * Creates a custom ClientHttpRequestFactory with specific timeout settings.
     *
     * @return the custom ClientHttpRequestFactory
     */
    ClientHttpRequestFactory customRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(jobeClientProperties.timeout())
                .withReadTimeout(jobeClientProperties.timeout());
        return ClientHttpRequestFactories.get(settings);
    }
}
