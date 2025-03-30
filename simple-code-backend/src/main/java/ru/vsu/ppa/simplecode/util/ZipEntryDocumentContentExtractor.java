package ru.vsu.ppa.simplecode.util;

import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;

@RequiredArgsConstructor
public class ZipEntryDocumentContentExtractor extends ZipEntryContentExtractor<Document> {

    private final DocumentBuilder xmlDocumentBuilder;

    @Override
    protected Document getContent(InputStream is) throws IOException {
        try {
            Document document = xmlDocumentBuilder.parse(is);
            document.getDocumentElement().normalize();
            return document;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
