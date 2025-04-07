package ru.vsu.ppa.simplecode.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ZipEntryStringContentExtractor extends ZipEntryContentExtractor<String> {

    public ZipEntryStringContentExtractor(ZipFile zip) {
        super(zip);
    }

    @Override
    protected String getContent(InputStream is) throws IOException {
        return new String(is.readAllBytes());
    }
}
