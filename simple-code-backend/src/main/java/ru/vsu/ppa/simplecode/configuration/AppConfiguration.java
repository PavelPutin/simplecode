package ru.vsu.ppa.simplecode.configuration;

import java.util.zip.ZipFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import ru.vsu.ppa.simplecode.service.JobeInABoxService;
import ru.vsu.ppa.simplecode.service.PolygonConverterService;
import ru.vsu.ppa.simplecode.util.PolygonZipAccessObject;
import ru.vsu.ppa.simplecode.util.ZipEntryByteArrayContentExtractor;
import ru.vsu.ppa.simplecode.util.ZipEntryContentExtractor;
import ru.vsu.ppa.simplecode.util.ZipEntryDocumentContentExtractor;
import ru.vsu.ppa.simplecode.util.ZipEntryStringContentExtractor;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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
    @SneakyThrows
    public PolygonConverterService polygonConverterService(JobeInABoxService jobeInABoxService,
                                                           ObjectMapper jacksonObjectMapper,
                                                           ProblemXmlParsingProperties problemXmlParsingProperties) {
        return new PolygonConverterService(jobeInABoxService) {
            @Override
            @SneakyThrows
            protected PolygonZipAccessObject getPolygonZipAccessObject(ZipFile zip) {
                val polygonZipAccessObject = polygonZipAccessObject(problemXmlParsingProperties, jacksonObjectMapper);
                polygonZipAccessObject.setZip(zip);
                return polygonZipAccessObject;
            }
        };
    }

    @Bean
    @Scope("prototype")
    public PolygonZipAccessObject polygonZipAccessObject(ProblemXmlParsingProperties problemXmlParsingProperties,
                                                         ObjectMapper jacksonObjectMapper) throws
            ParserConfigurationException {
        return new PolygonZipAccessObject(documentExtractor(),
                                          stringExtractor(),
                                          byteExtractor(),
                                          xPath(),
                                          problemXmlParsingProperties,
                                          jacksonObjectMapper);
    }

    @Bean
    @Scope("prototype")
    public ZipEntryContentExtractor<String> stringExtractor() {
        return new ZipEntryStringContentExtractor();
    }

    @Bean
    @Scope("prototype")
    public ZipEntryContentExtractor<byte[]> byteExtractor() {
        return new ZipEntryByteArrayContentExtractor();
    }

    @Bean
    @Scope("prototype")
    public ZipEntryContentExtractor<Document> documentExtractor() throws ParserConfigurationException {
        return new ZipEntryDocumentContentExtractor(xmlDocumentBuilder());
    }
}
