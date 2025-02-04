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
            return switch (source.toUpperCase()) {
                case "MANUAL" -> MANUAL;
                case "GENERATED" -> GENERATED;
                default -> throw new IllegalArgumentException("Unknown method: " + source);
            };
        }
    }
}
