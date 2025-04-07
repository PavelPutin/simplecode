package ru.vsu.ppa.simplecode.util;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class ZipEntryByteArrayContentExtractorObjectProvider
        extends ZipEntryContentExtractorObjectProvider<byte[]> {

    public ZipEntryByteArrayContentExtractorObjectProvider(ObjectProvider<ZipEntryContentExtractor<byte[]>> objectProvider) {
        super(objectProvider);
    }
}
