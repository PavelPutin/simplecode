package ru.vsu.ppa.simplecode.util;

import java.util.zip.ZipFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import ru.vsu.ppa.simplecode.configuration.ProblemXmlParsingProperties;
import javax.xml.xpath.XPath;

@Component
@RequiredArgsConstructor
public class PolygonZipAccessObjectProvider {

    private final ZipEntryDocumentContentExtractorObjectProvider documentExtractorProvider;
    private final ZipEntryStringContentExtractorObjectProvider stringExtractorProvider;
    private final ZipEntryByteArrayContentExtractorObjectProvider byteExtractorProvider;
    private final XPath xPath;
    private final ProblemXmlParsingProperties problemXmlParsingProperties;
    private final ObjectMapper jacksonObjectMapper;
    private final ObjectProvider<PolygonZipAccessObject> polygonZipAccessObjectProvider;

    public PolygonZipAccessObject getZipAccessObject(ZipFile zip) {
        ZipEntryContentExtractor<Document> documentExtractor = documentExtractorProvider.getExtractor(zip);
        ZipEntryContentExtractor<String> stringExtractor = stringExtractorProvider.getExtractor(zip);
        ZipEntryContentExtractor<byte[]> byteExtractor = byteExtractorProvider.getExtractor(zip);
        return polygonZipAccessObjectProvider.getObject(documentExtractor,
                                                        stringExtractor,
                                                        byteExtractor,
                                                        xPath,
                                                        problemXmlParsingProperties,
                                                        jacksonObjectMapper);
    }
}
