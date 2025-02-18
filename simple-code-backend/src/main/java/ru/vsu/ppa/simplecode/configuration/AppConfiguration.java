package ru.vsu.ppa.simplecode.configuration;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
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
public class AppConfiguration {

    @Value("${jobe.client.base-url}")
    private String jobeBaseUrl;

    @Value("${jobe.client.timeout}")
    private Duration jobeConnectionTimeout;

    /**
     * Creates a RestClient bean for making HTTP requests to the Jobe API.
     *
     * @return the RestClient bean
     */
    @Bean
    public RestClient jobeRestClient() {
        return RestClient.builder()
                .baseUrl(jobeBaseUrl)
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
                .withConnectTimeout(jobeConnectionTimeout)
                .withReadTimeout(jobeConnectionTimeout);
        return ClientHttpRequestFactories.get(settings);
    }
}
