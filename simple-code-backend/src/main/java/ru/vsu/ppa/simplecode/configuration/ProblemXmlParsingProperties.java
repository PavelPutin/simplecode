package ru.vsu.ppa.simplecode.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
public record ProblemXmlParsingProperties(
        @Value("${application.polygon.problem.name.xpath}")
        String problemNameXPath,
        @Value("${application.polygon.problem.name.attribute}")
        String problemNameAttribute,
        @Value("${application.polygon.problem.name.default}")
        String problemNameDefault,
        @Value("${application.polygon.problem.time-limit-millis.xpath}")
        String timeLimitMillisXPath,
        @Value("${application.polygon.problem.time-limit-millis.default}")
        int timeLimitMillisDefault,
        @Value("${application.polygon.problem.memory-limit.xpath}")
        String memoryLimitXPath,
        @Value("${application.polygon.problem.memory-limit.default}")
        DataSize memoryLimitDefault,
        @Value("${application.polygon.problem.executables.main-solution.xpath}")
        String mainSolutionSourceXPath,
        @Value("${application.polygon.problem.executables.other.xpath}")
        String otherExecutableSourcesXPath,
        @Value("${application.polygon.problem.executables.path-attribute}")
        String executablePathAttribute,
        @Value("${application.polygon.problem.executables.language-attribute}")
        String executableLanguageAttribute,
        @Value("${application.polygon.problem.test-sets.xpath}")
        String testSetsXpath,
        @Value("${application.polygon.problem.test-sets.name.attribute}")
        String testSetsNameAttribute,
        @Value("${application.polygon.problem.test-sets.input-path-pattern.xpath}")
        String pathPatternXpath,
        @Value("${application.polygon.problem.test-sets.tests.xpath}")
        String testSetsTestsXpath,
        @Value("${application.polygon.problem.test-sets.tests.sample.attribute}")
        String testSetsTestSampleAttribute,
        @Value("${application.polygon.problem.test-sets.tests.method.attribute}")
        String testSetsTestMethodAttribute,
        @Value("${application.polygon.problem.test-sets.tests.cmd.attribute}")
        String testSetsTestCmdAttribute
) {

}
