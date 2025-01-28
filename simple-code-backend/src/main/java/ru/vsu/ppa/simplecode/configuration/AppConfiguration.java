package ru.vsu.ppa.simplecode.configuration;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.time.Duration;

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
    private ClientHttpRequestFactory customRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(jobeClientProperties.timeout())
                .withReadTimeout(jobeClientProperties.timeout());
        return ClientHttpRequestFactories.get(settings);
    }

    /**
     * Creates a new instance of DocumentBuilder for XML documents.
     *
     * @return a new DocumentBuilder instance
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     */
    @Bean
    public DocumentBuilder xmlDocumentBuilder() throws ParserConfigurationException {
        val factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder();
    }

    /**
     * Creates a new instance of XPath.
     *
     * @return a new XPath instance
     */
    @Bean
    public XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }
}
