package ru.vsu.ppa.simplecode.util;

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Класс, предоставляющий вспомогательные методы для работы с путями файлов.
 */
public class PathHelper {

    /**
     * Преобразует путь файла в строку в формате Unix.
     *
     * @param path путь файла
     * @return строка, представляющая путь файла в формате Unix
     * @throws RuntimeException если разделитель пути неизвестен
     */
    public static String toUnixString(Path path) {
        return switch (FileSystems.getDefault().getSeparator()) {
            case "\\" -> path.toString().replace('\\', '/');
            case "/" -> path.toString();
            default -> throw new RuntimeException("Unknown separator");
        };
    }

    public static String getFileNameWithoutExtension(Path path) {
        if (path == null) {
            return null;
        }
        String fileName = path.getFileName().toString();
        return fileName.replaceAll("(?<!^)[.][^.]*$", "");
    }
}
