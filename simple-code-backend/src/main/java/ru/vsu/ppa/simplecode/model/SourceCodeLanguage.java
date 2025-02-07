package ru.vsu.ppa.simplecode.model;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Перечисление, представляющее языки исходного кода.
 */
public enum SourceCodeLanguage {

    /**
     * Язык C.
     */
    C(singletonList("c.gcc"), "c"),

    /**
     * Язык C++.
     */
    CPP(singletonList("cpp.gcc13-64-winlibs-g++20"), "cpp"),

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

    /**
     * Список обозначений языка в системе Polygon.
     */
    private List<String> polygonNotation;

    /**
     * Обозначение языка в системе Jobe.
     */
    private String jobeNotation;

    /**
     * Конструктор перечисления SourceCodeLanguage.
     *
     * @param polygonNotation список обозначений языка в системе Polygon
     * @param jobeNotation обозначение языка в системе Jobe
     */
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
        return Arrays.stream(SourceCodeLanguage.values())
                .filter(language -> language.getPolygonNotation()
                        .contains(polygonNotation))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not supported language"));
    }

    /**
     * Возвращает список обозначений языка в системе Polygon.
     *
     * @return список обозначений языка в системе Polygon
     */
    public List<String> getPolygonNotation() {
        return polygonNotation;
    }

    /**
     * Возвращает обозначение языка в системе Jobe.
     *
     * @return обозначение языка в системе Jobe
     */
    public String getJobeNotation() {
        return jobeNotation;
    }
}

