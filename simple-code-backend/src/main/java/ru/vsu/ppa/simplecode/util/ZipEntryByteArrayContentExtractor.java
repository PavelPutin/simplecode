package ru.vsu.ppa.simplecode.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class ZipEntryByteArrayContentExtractor extends ZipEntryContentExtractor<byte[]> {

    public ZipEntryByteArrayContentExtractor(ZipFile zip) {
        super(zip);
    }

    @Override
    protected byte[] getContent(InputStream is) throws IOException {
        return is.readAllBytes();
    }
}
