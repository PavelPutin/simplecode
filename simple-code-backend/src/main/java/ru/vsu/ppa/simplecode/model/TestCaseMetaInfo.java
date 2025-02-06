package ru.vsu.ppa.simplecode.model;

public record TestCaseMetaInfo(
        String testSetName,
        String pathPattern,
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
