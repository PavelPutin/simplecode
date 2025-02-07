package ru.vsu.ppa.simplecode.model;

import java.nio.file.Path;

public record TestCaseMetaInfo(
        String testSetName,
        int number,
        Path stdinSource,
        Path expectedSource,
        boolean sample,
        TestCaseMetaInfo.Method method,
        String generationCommand) {

    public enum Method {
        MANUAL,
        GENERATED;

        public static Method parse(String source) {
            return Method.valueOf(source.toUpperCase());
        }
    }
}
