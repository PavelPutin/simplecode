package ru.vsu.ppa.simplecode.model;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import static java.util.Collections.singletonList;

/**
 * Перечисление, представляющее языки исходного кода.
 */
@Getter
@Log4j2
public enum SourceCodeLanguage {

    /**
     * Язык C.
     */
    C(singletonList("c.gcc"), "c"),

    /**
     * Язык C++.
     */
    CPP(List.of("cpp.g++14", "cpp.g++17", "cpp.gcc13-64-winlibs-g++20", "cpp.gcc14-64-msys2-g++23"), "cpp"),

    /**
     * Язык Java.
     */
    JAVA(List.of("java21", "java8"), "java"),

    /**
     * Язык PHP.
     */
    PHP(singletonList("php.5"), "php"),

    /**
     * Язык Python 3.
     */
    PYTHON3(List.of("python.3", "python.pypy3-64"), "python3");

    private final List<String> polygonNotation;

    /**
     * Обозначение языка в системе Jobe.
     * -- GETTER --
     *  Возвращает обозначение языка в системе Jobe.
     *
     * @return обозначение языка в системе Jobe
     */
    private final String jobeNotation;

    SourceCodeLanguage(List<String> polygonNotation, String jobeNotation) {
        this.polygonNotation = polygonNotation;
        this.jobeNotation = jobeNotation;
    }

    /**
     * Возвращает язык исходного кода по обозначению в системе Polygon.
     *
     * @param polygonNotation обозначение языка в системе Polygon
     * @return язык исходного кода
     * @throws IllegalArgumentException если язык не поддерживается
     */
    public static SourceCodeLanguage getFromPolygonNotation(String polygonNotation) {
        log.debug(polygonNotation);
        return Arrays.stream(SourceCodeLanguage.values())
                .filter(language -> language.getPolygonNotation()
                        .contains(polygonNotation))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not supported language"));
    }

}

