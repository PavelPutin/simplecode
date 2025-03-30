package ru.vsu.ppa.simplecode.util;

import java.io.IOException;
import java.io.InputStream;

public class ZipEntryByteArrayContentExtractor extends ZipEntryContentExtractor<byte[]> {

    @Override
    protected byte[] getContent(InputStream is) throws IOException {
        return is.readAllBytes();
    }
}
