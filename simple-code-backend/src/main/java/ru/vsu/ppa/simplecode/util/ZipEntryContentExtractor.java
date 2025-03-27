package ru.vsu.ppa.simplecode.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.zip.ZipFile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import ru.vsu.ppa.simplecode.service.PolygonPackageIncomplete;

@RequiredArgsConstructor
public abstract class ZipEntryContentExtractor<T> {
    private final ZipFile zip;

    @SneakyThrows
    public T extract(Path pathToEntry) {
        String fixedPath = PathHelper.toUnixString(pathToEntry);
        val extractingEntry = zip.getEntry(fixedPath);
        if (extractingEntry == null) {
            throw new PolygonPackageIncomplete(MessageFormat.format("No entry {0} in the zip file", fixedPath));
        }
        try (val is = zip.getInputStream(extractingEntry)) {
            // TODO: fix big files problem
            return getContent(is);
        }
    }

    protected abstract T getContent(InputStream is) throws IOException;
}
