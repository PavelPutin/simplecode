package ru.vsu.ppa.simplecode.util;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class ZipEntryDocumentContentExtractorObjectProvider
        extends ZipEntryContentExtractorObjectProvider<Document> {

    public ZipEntryDocumentContentExtractorObjectProvider(ObjectProvider<ZipEntryContentExtractor<Document>> objectProvider) {
        super(objectProvider);
    }
}
