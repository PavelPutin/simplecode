package ru.vsu.ppa.simplecode.util;

import java.util.zip.ZipFile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public abstract class ZipEntryContentExtractorObjectProvider<T> {
    private final ObjectProvider<ZipEntryContentExtractor<T>> objectProvider;

    public ZipEntryContentExtractor<T> getExtractor(ZipFile zip) {
        return objectProvider.getObject(zip);
    }
}
