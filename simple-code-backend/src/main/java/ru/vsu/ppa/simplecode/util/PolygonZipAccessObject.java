package ru.vsu.ppa.simplecode.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import ru.vsu.ppa.simplecode.configuration.ProblemXmlParsingProperties;
import ru.vsu.ppa.simplecode.model.ExecutableMetaInfo;
import ru.vsu.ppa.simplecode.model.PolygonTestcase;
import ru.vsu.ppa.simplecode.model.ProgramSourceCode;
import ru.vsu.ppa.simplecode.model.SourceCodeLanguage;
import ru.vsu.ppa.simplecode.model.Statement;
import ru.vsu.ppa.simplecode.model.StatementFile;
import ru.vsu.ppa.simplecode.model.TaskMetaInfo;
import ru.vsu.ppa.simplecode.model.TestCaseMetaInfo;
import javax.xml.xpath.XPath;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PolygonZipAccessObject {

    private final ZipEntryContentExtractor<Document> documentExtractor;
    private final ZipEntryContentExtractor<String> stringExtractor;
    private final ZipEntryContentExtractor<byte[]> byteExtractor;
    private final XPath xPath;
    private final ProblemXmlParsingProperties problemXmlParsingProperties;
    private final ObjectMapper jacksonObjectMapper;

    private final TaskMetaInfo metaInfo;

    public PolygonZipAccessObject(ZipEntryDocumentContentExtractor documentExtractor,
                                  ZipEntryStringContentExtractor stringExtractor,
                                  ZipEntryByteArrayContentExtractor byteExtractor,
                                  XPath xPath,
                                  ProblemXmlParsingProperties problemXmlParsingProperties,
                                  ObjectMapper jacksonObjectMapper) {
        this.documentExtractor = documentExtractor;
        this.stringExtractor = stringExtractor;
        this.byteExtractor = byteExtractor;
        this.xPath = xPath;
        this.problemXmlParsingProperties = problemXmlParsingProperties;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.metaInfo = extractTaskMetaInfo();
    }

    public Statement extractStatement() throws JsonProcessingException {
        val json = stringExtractor.extract(metaInfo.statementPath().resolve("problem-properties.json"));
        return jacksonObjectMapper.readValue(json, Statement.class);
    }

    public List<StatementFile> extractImagesFromStatement(Statement statement) {
        return Stream.of(statement.legend(), statement.input(), statement.output(), statement.notes())
                .map(this::extractImages)
                .flatMap(List::stream)
                .toList();
    }

    public ProgramSourceCode extractMainSolution() {
        return new ProgramSourceCode(stringExtractor.extract(metaInfo.mainSolution().path()),
                                     metaInfo.mainSolution().language());
    }

    public Map<String, ProgramSourceCode> extractGenerators() {
        Map<String, ProgramSourceCode> generators = new HashMap<>();
        for (var generator : metaInfo.generators()) {
            val name = PathHelper.getFileNameWithoutExtension(generator.path().getFileName());
            val content = stringExtractor.extract(generator.path());
            val generatorSourceCode = new ProgramSourceCode(content, generator.language());
            generators.put(name, generatorSourceCode);
        }
        return generators;
    }

    public List<PolygonTestcase> extractTestCases() {
        return metaInfo.testCases().stream().map(PolygonTestcase::new).toList();
    }

    public String extractName() {
        return metaInfo.name();
    }

    public int extractTimeLimit() {
        return metaInfo.timeLimit();
    }

    public long extractMemoryLimit() {
        return metaInfo.memoryLimit().toMegabytes();
    }

    private List<StatementFile> extractImages(String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        Pattern includeGraphics = Pattern.compile("\\\\includegraphics.*\\{(.*)}");
        Matcher graphics = includeGraphics.matcher(text);
        return graphics.results().map(r -> r.group(1)).map(name -> {
            byte[] data = byteExtractor.extract(metaInfo.statementPath().resolve(name));
            data = Base64.getEncoder().encode(data);
            return new StatementFile(name, "/", "base64", new String(data));
        }).toList();
    }

    public Optional<String> extractStdin(PolygonTestcase testCase) {
        try {
            return Optional.of(stringExtractor.extract(testCase.getMetaInfo().stdinSource()).trim());
        } catch (PolygonPackageIncomplete e) {
            return Optional.empty();
        }
    }

    public Optional<String> extractExpected(PolygonTestcase testCase) {
        try {
            return Optional.of(stringExtractor.extract(testCase.getMetaInfo().expectedSource()).trim());
        } catch (PolygonPackageIncomplete e) {
            return Optional.empty();
        }
    }

    @SneakyThrows
    private TaskMetaInfo extractTaskMetaInfo() {
        val problemXml = Paths.get("problem.xml");
        var docHelper = new XmlNodeHelper(documentExtractor.extract(problemXml), xPath);
        val taskName = docHelper
                .getAttributeValue(problemXmlParsingProperties.name().xpath(),
                                   problemXmlParsingProperties.name().attribute())
                .orElse(problemXmlParsingProperties.name().defaultValue());
        val timeLimitMillis = docHelper
                .getDouble(problemXmlParsingProperties.timeLimitMillis().xpath())
                .map(Double::intValue)
                .orElse(problemXmlParsingProperties.timeLimitMillis().defaultValue());
        val memoryLimit = docHelper
                .getDouble(problemXmlParsingProperties.memoryLimit().xpath())
                .map(Double::intValue)
                .map(DataSize::ofBytes)
                .orElse(problemXmlParsingProperties.memoryLimit().defaultValue());
        val statementPath = docHelper
                .getAttributeValue(problemXmlParsingProperties.statement().xpath(),
                                   problemXmlParsingProperties.statement().attribute())
                .map(Paths::get)
                .map(Path::getParent)
                .orElseThrow();
        val solutionSourceElement = docHelper
                .getNode(problemXmlParsingProperties.executables().mainSolution().xpath())
                .orElseThrow();
        val mainSolution = extractExecutable(solutionSourceElement);
        List<ExecutableMetaInfo> executablesMetaInfo = docHelper
                .getNodeList(problemXmlParsingProperties.executables().other().xpath())
                .stream()
                .map(this::extractExecutable)
                .toList();
        List<TestCaseMetaInfo> testCasesMetaInfo = docHelper.getNodeList(problemXmlParsingProperties.testSets().xpath())
                .stream()
                .map(this::extractTestSet)
                .flatMap(Collection::stream)
                .toList();
        return new TaskMetaInfo(taskName,
                                timeLimitMillis,
                                memoryLimit,
                                statementPath,
                                mainSolution,
                                executablesMetaInfo,
                                testCasesMetaInfo);
    }

    @SneakyThrows
    private ExecutableMetaInfo extractExecutable(Node node) {
        val nodeHelper = new XmlNodeHelper(node, xPath);
        val pathToSource = nodeHelper
                .getAttributeValue(problemXmlParsingProperties.executables().pathAttribute())
                .map(Paths::get)
                .orElseThrow();
        val language = nodeHelper
                .getAttributeValue(problemXmlParsingProperties.executables().languageAttribute())
                .map(SourceCodeLanguage::getFromPolygonNotation)
                .orElseThrow();
        return new ExecutableMetaInfo(pathToSource, language);
    }

    /**
     * Извлекает набор тестов из узла XML.
     *
     * @param testSet узел XML, представляющий набор тестов
     * @return список метаинформации о тестовых случаях
     * @throws RuntimeException если произошла ошибка при извлечении набора тестов
     */
    @SneakyThrows
    private List<TestCaseMetaInfo> extractTestSet(Node testSet) {
        val nodeHelper = new XmlNodeHelper(testSet, xPath);
        val testSetName = nodeHelper
                .getAttributeValue(problemXmlParsingProperties.testSets().name().attribute())
                .orElseThrow();
        val stdinPathPattern = nodeHelper
                .getString(problemXmlParsingProperties.testSets().stdinPathPattern().xpath())
                .orElse(testSetName + "/%02d");
        val expectedPathPattern = nodeHelper
                .getString(problemXmlParsingProperties.testSets().expectedPathPattern().xpath())
                .orElse(testSetName + "/%02d.a");
        val tests = nodeHelper.getNodeList(problemXmlParsingProperties.testSets().tests().xpath());

        List<TestCaseMetaInfo> testCasesMetaInfo = new ArrayList<>();
        for (int testNumber = 0; testNumber < tests.size(); testNumber++) {
            val testHelper = new XmlNodeHelper(tests.get(testNumber), xPath);
            val stdinSource = Paths.get(stdinPathPattern.formatted(testNumber + 1));
            val expectedSource = Paths.get(expectedPathPattern.formatted(testNumber + 1));
            val sample = testHelper
                    .getAttributeValue(problemXmlParsingProperties.testSets().tests().sample().attribute())
                    .map(Boolean::parseBoolean)
                    .orElse(false);
            val method = testHelper
                    .getAttributeValue(problemXmlParsingProperties.testSets().tests().method().attribute())
                    .map(TestCaseMetaInfo.Method::parse)
                    .orElse(TestCaseMetaInfo.Method.MANUAL);
            val generationCommand = testHelper
                    .getAttributeValue(problemXmlParsingProperties.testSets().tests().cmd().attribute())
                    .orElse(null);
            testCasesMetaInfo.add(new TestCaseMetaInfo(testSetName,
                                                       testNumber,
                                                       stdinSource,
                                                       expectedSource,
                                                       sample,
                                                       method,
                                                       generationCommand));
        }
        return testCasesMetaInfo;
    }
}
