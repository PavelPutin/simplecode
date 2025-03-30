package ru.vsu.ppa.simplecode.util;

import java.io.IOException;
import java.io.InputStream;

public class ZipEntryStringContentExtractor extends ZipEntryContentExtractor<String> {
    @Override
    protected String getContent(InputStream is) throws IOException {
        return new String(is.readAllBytes());
    }
}
