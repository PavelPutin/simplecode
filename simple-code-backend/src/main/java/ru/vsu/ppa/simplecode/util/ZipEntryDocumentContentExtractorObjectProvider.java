package ru.vsu.ppa.simplecode.util;

import java.util.zip.ZipFile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;

@Component
public class ZipEntryDocumentContentExtractorObjectProvider
        extends ZipEntryContentExtractorObjectProvider<Document> {

    private final DocumentBuilder xmlDocumentBuilder;

    public ZipEntryDocumentContentExtractorObjectProvider(
            ObjectProvider<ZipEntryContentExtractor<Document>> objectProvider,
            DocumentBuilder xmlDocumentBuilder) {
        super(objectProvider);
        this.xmlDocumentBuilder = xmlDocumentBuilder;
    }

    @Override
    public ZipEntryContentExtractor<Document> getExtractor(ZipFile zip) {
        return objectProvider.getObject(zip, xmlDocumentBuilder);
    }
}
