package ru.vsu.ppa.simplecode.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties("application.polygon.problem")
public record ProblemXmlParsingProperties(
        XpathAttributeDefaultValue<String> name,
        XpathDefaultValue<Integer> timeLimitMillis,
        XpathDefaultValue<DataSize> memoryLimit,
        XpathAttribute statement,
        Executables executables,
        TestSets testSets) {

    public record XpathAttributeDefaultValue<T>(String xpath, String attribute, T defaultValue) {}

    public record XpathAttribute(String xpath, String attribute) {}

    public record XpathDefaultValue<T>(String xpath, T defaultValue) {}

    public record Xpath(String xpath) {}

    public record Attribute(String attribute) {}

    public record Executables(Xpath mainSolution, Xpath other, String pathAttribute, String languageAttribute) {}

    public record TestSets(String xpath,
                           Attribute name,
                           Xpath stdinPathPattern,
                           Xpath expectedPathPattern,
                           Test tests) {}

    public record Test(String xpath, Attribute sample, Attribute method, Attribute cmd) {}
}
