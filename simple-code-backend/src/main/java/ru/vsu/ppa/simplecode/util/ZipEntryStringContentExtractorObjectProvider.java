package ru.vsu.ppa.simplecode.util;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class ZipEntryStringContentExtractorObjectProvider extends ZipEntryContentExtractorObjectProvider<String> {

    public ZipEntryStringContentExtractorObjectProvider(ObjectProvider<ZipEntryContentExtractor<String>> objectProvider) {
        super(objectProvider);
    }
}
