package ru.vsu.ppa.simplecode.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.vsu.ppa.simplecode.configuration.ProblemXmlParsingProperties;
import ru.vsu.ppa.simplecode.model.Task;
import ru.vsu.ppa.simplecode.model.TestCaseMetaInfo;
import ru.vsu.ppa.simplecode.util.PathHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Log4j2
@RequiredArgsConstructor
public class PolygonConverterService {

    private final DocumentBuilder xmlDocumentBuilder;
    private final XPath xPath;
    private final ProblemXmlParsingProperties problemXmlParsingProperties;

    private record TaskMetaInfo(String name,
                                int timeLimit,
                                DataSize memoryLimit,
                                ExecutableMetaInfo mainSolution,
                                List<ExecutableMetaInfo> generators,
                                List<TestCaseMetaInfo> testCases) {}

    private record ExecutableMetaInfo(Path path, String Language) {}

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

            String mainSolution = extractEntryContent(zip, metaInfo.mainSolution.path());
            log.debug("Main solution: {}", mainSolution);

            List<String> generators = metaInfo.generators()
                    .stream()
                    .map(g -> extractEntryContent(zip, g.path()))
                    .toList();
            IntStream.range(0, generators.size())
                    .forEach(i -> log.debug("Generator {}: {}", i + 1, generators.get(i)));
        }
        return null;
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
        val tmpdir = Files.createTempDirectory(null)
                .toString();
        val zipPath = Paths.get(tmpdir, polygonPackage.getOriginalFilename());
        polygonPackage.transferTo(zipPath);
        return new ZipFile(zipPath.toFile());
    }

    @SneakyThrows
    private TaskMetaInfo extractTaskMetaInfo(ZipFile zip) {
        val problemXmlDescription = zip.getEntry("problem.xml");
        if (problemXmlDescription == null) {
            throw new PolygonPackageIncomplete("No problem.xml entry in the zip file");
        }

        val document = getDocument(zip, problemXmlDescription);

        val taskNameElement = (Node) xPath.evaluate(problemXmlParsingProperties.problemNameXPath(),
                                                    document,
                                                    XPathConstants.NODE);
        val taskName = Optional.ofNullable(taskNameElement)
                .map(element -> element.getAttributes()
                        .getNamedItem(problemXmlParsingProperties.problemNameAttribute()))
                .map(Node::getNodeValue)
                .orElse(problemXmlParsingProperties.problemNameDefault());

        val timeLimitMillisElement = (Double) xPath.evaluate(problemXmlParsingProperties.timeLimitMillisXPath(),
                                                             document,
                                                             XPathConstants.NUMBER);
        val timeLimitMillis = Optional.of(timeLimitMillisElement)
                .filter(Double::isFinite)
                .map(Double::intValue)
                .orElse(problemXmlParsingProperties.timeLimitMillisDefault());

        val memoryLimitElement = (Double) xPath.evaluate(problemXmlParsingProperties.memoryLimitXPath(),
                                                         document,
                                                         XPathConstants.NUMBER);
        val memoryLimit = Optional.of(memoryLimitElement)
                .filter(Double::isFinite)
                .map(Double::intValue)
                .map(DataSize::ofBytes)
                .orElse(problemXmlParsingProperties.memoryLimitDefault());

        val solutionSourceElement = (Node) xPath.evaluate(problemXmlParsingProperties.mainSolutionSourceXPath(),
                                                          document,
                                                          XPathConstants.NODE);
        if (solutionSourceElement == null) {
            throw PolygonProblemXMLIncomplete.tagNotFound(problemXmlParsingProperties.mainSolutionSourceXPath());
        }

        val mainSolution = extractExecutable(solutionSourceElement,
                                             problemXmlParsingProperties.mainSolutionSourceXPath());

        val executables = (NodeList) xPath.evaluate(problemXmlParsingProperties.otherExecutableSourcesXPath(),
                                                    document,
                                                    XPathConstants.NODESET);

        List<ExecutableMetaInfo> executablesMetaInfo = IntStream.range(0, executables.getLength())
                .mapToObj(executables::item)
                .map(n -> extractExecutable(n, problemXmlParsingProperties.otherExecutableSourcesXPath()))
                .toList();

        val testSets = (NodeList) xPath.evaluate(problemXmlParsingProperties.testSetsXpath(),
                                                 document,
                                                 XPathConstants.NODESET);
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

    private ExecutableMetaInfo extractExecutable(Node node, String nodeXPath) {
        val pathToSource = Optional.of(node)
                .map(element -> element.getAttributes()
                        .getNamedItem(problemXmlParsingProperties.executablePathAttribute()))
                .map(Node::getNodeValue)
                .map(Paths::get)
                .orElseThrow(() -> PolygonProblemXMLIncomplete.tagWithAttributeNotFound(nodeXPath,
                                                                                        problemXmlParsingProperties.executablePathAttribute()));
        val language = Optional.of(node)
                .map(element -> element.getAttributes()
                        .getNamedItem(problemXmlParsingProperties.executableLanguageAttribute()))
                .map(Node::getNodeValue)
                .orElseThrow(() -> PolygonProblemXMLIncomplete.tagWithAttributeNotFound(nodeXPath,
                                                                                        problemXmlParsingProperties.executableLanguageAttribute()));
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
        val testSetName = testSet.getAttributes()
                .getNamedItem(problemXmlParsingProperties.testSetsNameAttribute())
                .getNodeValue();
        val pathPattern = Optional.ofNullable((String) xPath.evaluate(problemXmlParsingProperties.pathPatternXpath(),
                                                                      testSet,
                                                                      XPathConstants.STRING))
                .orElse(testSetName + "/%02d");
        val tests = (NodeList) xPath.evaluate(problemXmlParsingProperties.testSetsTestsXpath(),
                                              testSet,
                                              XPathConstants.NODESET);

        List<TestCaseMetaInfo> testCasesMetaInfo = new ArrayList<>();
        for (int testNumber = 0; testNumber < tests.getLength(); testNumber++) {
            val test = tests.item(testNumber)
                    .getAttributes();

            boolean sample = Optional.ofNullable(test.getNamedItem(problemXmlParsingProperties.testSetsTestSampleAttribute()))
                    .map(Node::getNodeValue)
                    .map(Boolean::parseBoolean)
                    .orElse(false);

            val method = Optional.ofNullable(test.getNamedItem(problemXmlParsingProperties.testSetsTestMethodAttribute()))
                    .map(Node::getNodeValue)
                    .map(TestCaseMetaInfo.Method::parse)
                    .orElse(TestCaseMetaInfo.Method.MANUAL);

            val generationCommand = Optional.ofNullable(test.getNamedItem(problemXmlParsingProperties.testSetsTestCmdAttribute()))
                    .map(Node::getNodeValue)
                    .orElse(null);

            testCasesMetaInfo.add(new TestCaseMetaInfo(testSetName, pathPattern, sample, method, generationCommand));
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
        document.getDocumentElement()
                .normalize();
        return document;
    }

}
