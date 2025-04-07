package ru.vsu.ppa.simplecode.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class ZipEntryStringContentExtractor extends ZipEntryContentExtractor<String> {

    public ZipEntryStringContentExtractor(ZipFile zip) {
        super(zip);
    }

    @Override
    protected String getContent(InputStream is) throws IOException {
        return new String(is.readAllBytes());
    }
}
