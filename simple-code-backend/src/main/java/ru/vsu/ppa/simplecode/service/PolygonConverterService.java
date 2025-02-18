package ru.vsu.ppa.simplecode.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ru.vsu.ppa.simplecode.configuration.ProblemXmlParsingProperties;
import ru.vsu.ppa.simplecode.model.PolygonTestcase;
import ru.vsu.ppa.simplecode.model.ProgramSourceCode;
import ru.vsu.ppa.simplecode.model.RunSpec;
import ru.vsu.ppa.simplecode.model.SourceCodeLanguage;
import ru.vsu.ppa.simplecode.model.Task;
import ru.vsu.ppa.simplecode.model.TestCaseMetaInfo;
import ru.vsu.ppa.simplecode.util.PathHelper;
import ru.vsu.ppa.simplecode.util.XmlNodeHelper;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;

@Service
@Log4j2
@RequiredArgsConstructor
public class PolygonConverterService {

    private final DocumentBuilder xmlDocumentBuilder;
    private final XPath xPath;
    private final ProblemXmlParsingProperties problemXmlParsingProperties;
    private final JobeInABoxService jobeInABoxService;

    /**
     * Converts a polygon package to a programming problem.
     *
     * @param polygonPackage the multipart file representing the polygon package
     * @return the converted programming problem
     */
    @SneakyThrows(IOException.class)
    public Task convertPolygonPackageToProgrammingProblem(MultipartFile polygonPackage) {
        try (ZipFile zip = multipartResolver(polygonPackage)) {
            val metaInfo = extractTaskMetaInfo(zip);
            log.debug("Task meta info: {}", metaInfo);

            val mainSolution = new ProgramSourceCode(extractEntryContent(zip, metaInfo.mainSolution.path()),
                                                     metaInfo.mainSolution.language());
            log.debug("Main solution: {}", mainSolution);

            Map<String, ProgramSourceCode> generators = mapGeneratorNames(metaInfo, zip);
            log.debug("Generators: {}", generators);

            List<PolygonTestcase> testCases = metaInfo.testCases().stream()
                    .map(PolygonTestcase::new)
                    .toList();

            List<RunSpec> stdinGenerationErrors = new ArrayList<>();
            List<RunSpec> expectedGenerationErrors = new ArrayList<>();

            testCases.forEach(testCase -> {
                log.debug("Test case: {}/{}",
                          testCase.getMetaInfo().testSetName(),
                          testCase.getMetaInfo().number() + 1);

                String stdin = getStdinValue(testCase, zip, generators, stdinGenerationErrors);
                testCase.setStdin(stdin);

                String expected = getExpectedValue(testCase, zip, mainSolution, expectedGenerationErrors);
                testCase.setExpected(expected);
            });

            log.debug("Test cases ({}):", testCases.size());
            testCases.forEach(testCase -> log.debug("Test case: {}", testCase));

            log.debug("Stdin generation errors ({}):", stdinGenerationErrors.size());
            stdinGenerationErrors.forEach(log::debug);

            log.debug("Expected generation errors ({}):", expectedGenerationErrors.size());
            expectedGenerationErrors.forEach(log::debug);

            return null;
        }
    }

    private String getExpectedValue(PolygonTestcase testCase,
                                    ZipFile zip,
                                    ProgramSourceCode mainSolution,
                                    List<RunSpec> expectedGenerationErrors) {
        String result = null;
        try {
            result = extractExpected(testCase, zip);
            log.debug("Extract expected value: {}", result);
        } catch (PolygonPackageIncomplete e) {
            if (testCase.getStdin() != null) {
                result = generateExpected(testCase, mainSolution, expectedGenerationErrors);
                log.debug("Generate expected value: {}", result);
            }
        }
        return result;
    }

    private String generateExpected(PolygonTestcase testCase,
                                    ProgramSourceCode mainSolution,
                                    List<RunSpec> expectedGenerationErrors) {
        val runSpec = new RunSpec(mainSolution.language().getJobeNotation(),
                                  mainSolution.content(),
                                  testCase.getStdin(),
                                  null);
        try {
            return jobeInABoxService.submitRun(runSpec);
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            expectedGenerationErrors.add(runSpec);
        }
        return null;
    }

    private String extractExpected(PolygonTestcase testCase, ZipFile zip) {
        return extractEntryContent(zip, testCase.getMetaInfo().expectedSource()).trim();
    }

    private String getStdinValue(PolygonTestcase testCase,
                                 ZipFile zip,
                                 Map<String, ProgramSourceCode> generators,
                                 List<RunSpec> stdinGenerationErrors) {
        String result = null;
        try {
            result = extractStdin(testCase, zip);
            log.debug("Extract stdin value: {}", result);
        } catch (PolygonPackageIncomplete e) {
            if (testCase.getMetaInfo().method() == TestCaseMetaInfo.Method.GENERATED) {
                result = generateStdin(testCase, generators, stdinGenerationErrors);
                log.debug("Generate stdin value: {}", result);
            }
        }
        return result;
    }

    private String generateStdin(PolygonTestcase testCase,
                                 Map<String, ProgramSourceCode> generators,
                                 List<RunSpec> stdinGenerationErrors) {
        val cmd = testCase.getMetaInfo().generationCommand();
        val tokens = cmd.split(" ");
        val generatorName = tokens[0];
        val generator = generators.get(generatorName);
        List<String> args = Arrays.asList(tokens).subList(1, tokens.length);
        val runSpec = new RunSpec(generator.language().getJobeNotation(),
                                  generator.content(),
                                  null,
                                  new RunSpec.Parameters(args));
        try {
            return jobeInABoxService.submitRun(runSpec);
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            stdinGenerationErrors.add(runSpec);
        }
        return null;
    }

    private String extractStdin(PolygonTestcase testCase, ZipFile zip) {
        return extractEntryContent(zip, testCase.getMetaInfo().stdinSource()).trim();
    }

    private Map<String, ProgramSourceCode> mapGeneratorNames(TaskMetaInfo metaInfo, ZipFile zip) {
        Map<String, ProgramSourceCode> generators = new HashMap<>();
        for (var generator : metaInfo.generators()) {
            val name = PathHelper.getFileNameWithoutExtension(generator.path().getFileName());
            val content = extractEntryContent(zip, generator.path());
            val generatorSourceCode = new ProgramSourceCode(content, generator.language());
            generators.put(name, generatorSourceCode);
        }
        return generators;
    }

    @SneakyThrows
    private String extractEntryContent(ZipFile zip, Path pathToEntry) {
        String fixedPath = PathHelper.toUnixString(pathToEntry);
        val extractingEntry = zip.getEntry(fixedPath);
        if (extractingEntry == null) {
            throw new PolygonPackageIncomplete(MessageFormat.format("No entry {0} in the zip file", fixedPath));
        }
        try (val is = zip.getInputStream(extractingEntry)) {
            // TODO: fix big files problem
            return new String(is.readAllBytes());
        }
    }

    /**
     * Resolves a multipart file into a zip file.
     *
     * @param polygonPackage the multipart file to resolve
     * @return the resolved zip file
     */
    @SneakyThrows(IOException.class)
    private ZipFile multipartResolver(MultipartFile polygonPackage) {
        val tempdir = Files.createTempDirectory(null).toString();
        val zipPath = Paths.get(tempdir, polygonPackage.getOriginalFilename());
        polygonPackage.transferTo(zipPath);
        return new ZipFile(zipPath.toFile());
    }

    @SneakyThrows
    private TaskMetaInfo extractTaskMetaInfo(ZipFile zip) {
        val problemXmlDescription = zip.getEntry("problem.xml");
        if (problemXmlDescription == null) {
            throw new PolygonPackageIncomplete("No problem.xml entry in the zip file");
        }

        var docHelper = new XmlNodeHelper(getDocument(zip, problemXmlDescription), xPath);

        val taskName = docHelper.getAttributeValue(problemXmlParsingProperties.name().xpath(),
                                                   problemXmlParsingProperties.name().attribute())
                .orElse(problemXmlParsingProperties.name().defaultValue());

        val timeLimitMillis = docHelper.getDouble(problemXmlParsingProperties.timeLimitMillis().xpath())
                .map(Double::intValue)
                .orElse(problemXmlParsingProperties.timeLimitMillis().defaultValue());

        val memoryLimit = docHelper.getDouble(problemXmlParsingProperties.memoryLimit().xpath())
                .map(Double::intValue)
                .map(DataSize::ofBytes)
                .orElse(problemXmlParsingProperties.memoryLimit().defaultValue());

        val solutionSourceElement = docHelper.getNode(problemXmlParsingProperties.executables().mainSolution().xpath())
                .orElseThrow(() -> PolygonProblemXMLIncomplete.tagNotFound(problemXmlParsingProperties.executables()
                                                                                   .mainSolution()
                                                                                   .xpath()));

        val mainSolution = extractExecutable(solutionSourceElement,
                                             problemXmlParsingProperties.executables().mainSolution().xpath());

        val executables = docHelper.getNodeList(problemXmlParsingProperties.executables().other().xpath());

        List<ExecutableMetaInfo> executablesMetaInfo = IntStream.range(0, executables.getLength())
                .mapToObj(executables::item)
                .map(n -> extractExecutable(n, problemXmlParsingProperties.executables().other().xpath()))
                .toList();

        val testSets = docHelper.getNodeList(problemXmlParsingProperties.testSets().xpath());
        List<TestCaseMetaInfo> testCasesMetaInfo = IntStream.range(0, testSets.getLength())
                .mapToObj(testSets::item)
                .map(this::extractTestSet)
                .flatMap(Collection::stream)
                .toList();

        // Return a new TaskMetaInfo object with the extracted task name and default values for other fields
        return new TaskMetaInfo(taskName,
                                timeLimitMillis,
                                memoryLimit,
                                mainSolution,
                                executablesMetaInfo,
                                testCasesMetaInfo);
    }

    @SneakyThrows
    private ExecutableMetaInfo extractExecutable(Node node, String nodeXPath) {
        val nodeHelper = new XmlNodeHelper(node, xPath);

        val pathToSource = nodeHelper.getAttributeValue(problemXmlParsingProperties.executables().pathAttribute())
                .map(Paths::get)
                .orElseThrow(() -> PolygonProblemXMLIncomplete
                        .tagWithAttributeNotFound(nodeXPath,
                                                  problemXmlParsingProperties.executables().pathAttribute()));

        val language = nodeHelper.getAttributeValue(problemXmlParsingProperties.executables().languageAttribute())
                .map(SourceCodeLanguage::getFromPolygonNotation)
                .orElseThrow(() -> PolygonProblemXMLIncomplete
                        .tagWithAttributeNotFound(nodeXPath,
                                                  problemXmlParsingProperties.executables().languageAttribute()));
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
        val testSetName = nodeHelper.getAttributeValue(problemXmlParsingProperties.testSets().name().attribute())
                .orElseThrow();
        val stdinPathPattern = nodeHelper.getString(problemXmlParsingProperties.testSets()
                                                            .stdinPathPattern().xpath())
                .orElse(testSetName + "/%02d");
        val expectedPathPattern = nodeHelper.getString(problemXmlParsingProperties.testSets()
                                                               .expectedPathPattern().xpath())
                .orElse(testSetName + "/%02d.a");
        val tests = nodeHelper.getNodeList(problemXmlParsingProperties.testSets().tests().xpath());

        List<TestCaseMetaInfo> testCasesMetaInfo = new ArrayList<>();
        for (int testNumber = 0; testNumber < tests.getLength(); testNumber++) {
            val testHelper = new XmlNodeHelper(tests.item(testNumber), xPath);

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

    /**
     * Получает документ из ZIP-файла.
     *
     * @param zip                   ZIP-файл, из которого нужно получить документ
     * @param problemXmlDescription ZIP-запись, представляющая XML-описание проблемы
     * @return документ, полученный из ZIP-файла
     * @throws SAXException если произошла ошибка при разборе XML-документа
     * @throws IOException  если произошла ошибка ввода-вывода
     */
    private Document getDocument(ZipFile zip, ZipEntry problemXmlDescription) throws SAXException, IOException {
        val document = xmlDocumentBuilder.parse(zip.getInputStream(problemXmlDescription));
        document.getDocumentElement().normalize();
        return document;
    }

    private record TaskMetaInfo(String name,
                                int timeLimit,
                                DataSize memoryLimit,
                                ExecutableMetaInfo mainSolution,
                                List<ExecutableMetaInfo> generators,
                                List<TestCaseMetaInfo> testCases) {}

    private record ExecutableMetaInfo(Path path, SourceCodeLanguage language) {}

}
