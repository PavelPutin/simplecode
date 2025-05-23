package ru.vsu.ppa.simplecode.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.vsu.ppa.simplecode.model.JobeRunAssetFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Configuration class for the application.
 */
@Log4j2
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
        return RestClient.builder().baseUrl(jobeClientProperties.baseUrl()).requestFactory(customRequestFactory())
                .build();
    }

    /**
     * Creates a custom ClientHttpRequestFactory with specific timeout settings.
     *
     * @return the custom ClientHttpRequestFactory
     */
    private ClientHttpRequestFactory customRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS.withConnectTimeout(
                jobeClientProperties.timeout()).withReadTimeout(jobeClientProperties.timeout());
        return ClientHttpRequestFactories.get(settings);
    }

    /**
     * Creates a new instance of DocumentBuilder for XML documents.
     *
     * @return a new DocumentBuilder instance
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     */
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
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

    @Bean
    public JobeRunAssetFile testLibHeaderFile() {
        val testLibHeaderFileName = "testlib.h";
        try (InputStream stream = new ClassPathResource(testLibHeaderFileName).getInputStream()) {
            val value = new String(Base64.getEncoder().encode(stream.readAllBytes()));
            return new JobeRunAssetFile("testlibheader", testLibHeaderFileName, true, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Semaphore jobeRunsSemaphore() {
        return new Semaphore(12, true);
    }
}
