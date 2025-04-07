package ru.vsu.ppa.simplecode.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ZipEntryDocumentContentExtractor extends ZipEntryContentExtractor<Document> {

    private final DocumentBuilder xmlDocumentBuilder;

    public ZipEntryDocumentContentExtractor(ZipFile zip, DocumentBuilder xmlDocumentBuilder) {
        super(zip);
        this.xmlDocumentBuilder = xmlDocumentBuilder;}

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
