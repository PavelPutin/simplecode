package ru.vsu.ppa.simplecode.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
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

    @Value("${application.polygon.problem.name.xpath}")
    private String problemNameXPath;
    @Value("${application.polygon.problem.name.attribute}")
    private String problemNameAttribute;
    @Value("${application.polygon.problem.name.default}")
    private String problemNameDefault;
    @Value("${application.polygon.problem.timeLimitMillis.xpath}")
    private String timeLimitMillisXPath;
    @Value("${application.polygon.problem.timeLimitMillis.default}")
    private int timeLimitMillisDefault;
    @Value("${application.polygon.problem.memoryLimit.xpath}")
    private String memoryLimitXPath;
    @Value("${application.polygon.problem.memoryLimit.default}")
    private DataSize memoryLimitDefault;
    @Value("${application.polygon.problem.solutionSource.xpath}")
    private String solutionSourceXPath;
    @Value("${application.polygon.problem.solutionSource.path-attribute}")
    private String solutionSourcePathAttribute;
    @Value("${application.polygon.problem.solutionSource.language-attribute}")
    private String solutionSourceLanguageAttribute;
    @Value("${application.polygon.problem.testSets.xpath}")
    private String testSetsXpath;
    @Value("${application.polygon.problem.testSets.name.attribute}")
    private String testSetsNameAttribute;
    @Value("${application.polygon.problem.testSets.input-path-pattern.xpath}")
    private String pathPatternXpath;
    @Value("${application.polygon.problem.testSets.tests.xpath}")
    private String testSetsTestsXpath;
    @Value("${application.polygon.problem.testSets.tests.sample.attribute}")
    private String testSetsTestSampleAttribute;
    @Value("${application.polygon.problem.testSets.tests.method.attribute}")
    private String testSetsTestMethodAttribute;
    @Value("${application.polygon.problem.testSets.tests.cmd.attribute}")
    private String testSetsTestCmdAttribute;

    private record TaskMetaInfo(String name, int timeLimit, DataSize memoryLimit, Path solutionSource,
                                String mainSolutionLanguage, List<TestCaseMetaInfo> testCases) {

    }

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

            String mainSolution = extractMainSolution(zip, metaInfo);
            log.debug("Main solution: {}", mainSolution);
        }
        return null;
    }

    private String extractMainSolution(ZipFile zip, TaskMetaInfo metaInfo) throws IOException {
        String pathToSolution = PathHelper.toUnixString(metaInfo.solutionSource());
        val mainSolutionEntry = zip.getEntry(pathToSolution);
        if (mainSolutionEntry == null) {
            throw new PolygonPackageIncomplete("No main solution entry in the zip file");
        }
        try (val is = zip.getInputStream(mainSolutionEntry)) {
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
        val tmpdir = Files.createTempDirectory(null).toString();
        val zipPath = Paths.get(tmpdir, polygonPackage.getOriginalFilename());
        polygonPackage.transferTo(zipPath);
        return new ZipFile(zipPath.toFile());
    }

    @SneakyThrows()
    private TaskMetaInfo extractTaskMetaInfo(ZipFile zip) {
        val problemXmlDescription = zip.getEntry("problem.xml");
        if (problemXmlDescription == null) {
            throw new PolygonPackageIncomplete("No problem.xml entry in the zip file");
        }

        val document = getDocument(zip, problemXmlDescription);

        val taskNameElement = (Node) xPath.evaluate(problemNameXPath, document, XPathConstants.NODE);
        val taskName = Optional.ofNullable(taskNameElement)
                .map(element -> element.getAttributes().getNamedItem(problemNameAttribute))
                .map(Node::getNodeValue)
                .orElse(problemNameDefault);

        val timeLimitMillisElement = (Double) xPath.evaluate(timeLimitMillisXPath, document, XPathConstants.NUMBER);
        val timeLimitMillis = Optional.of(timeLimitMillisElement)
                .filter(Double::isFinite)
                .map(Double::intValue)
                .orElse(timeLimitMillisDefault);

        val memoryLimitElement = (Double) xPath.evaluate(memoryLimitXPath, document, XPathConstants.NUMBER);
        val memoryLimit = Optional.of(memoryLimitElement)
                .filter(Double::isFinite)
                .map(Double::intValue)
                .map(DataSize::ofBytes)
                .orElse(memoryLimitDefault);

        val solutionSourceElement = (Node) xPath.evaluate(solutionSourceXPath, document, XPathConstants.NODE);
        if (solutionSourceElement == null) {
            throw new PolygonProblemXMLIncomplete("Not found: " + solutionSourceXPath);
        }

        val solutionSource = Optional.of(solutionSourceElement)
                .map(element -> element.getAttributes().getNamedItem(solutionSourcePathAttribute))
                .map(Node::getNodeValue)
                .map(Paths::get)
                .orElseThrow(() -> new PolygonProblemXMLIncomplete("Not found: %s[@%s]".formatted(solutionSourceXPath, solutionSourcePathAttribute)));
        val language = Optional.of(solutionSourceElement)
                .map(element -> element.getAttributes().getNamedItem(solutionSourceLanguageAttribute))
                .map(Node::getNodeValue)
                .orElseThrow(() -> new PolygonProblemXMLIncomplete("Not found: %s[@%s]".formatted(solutionSourceXPath, solutionSourceLanguageAttribute)));

        NodeList testSets = (NodeList) xPath.evaluate(testSetsXpath, document, XPathConstants.NODESET);
        List<TestCaseMetaInfo> testCasesMetaInfo = IntStream.range(0, testSets.getLength())
                .mapToObj(testSets::item)
                .map(this::extractTestSet)
                .flatMap(Collection::stream)
                .toList();

        // Return a new TaskMetaInfo object with the extracted task name and default values for other fields
        return new TaskMetaInfo(taskName, timeLimitMillis, memoryLimit, solutionSource, language, testCasesMetaInfo);
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
        String testSetName = testSet.getAttributes().getNamedItem(testSetsNameAttribute).getNodeValue();
        String pathPattern = Optional.ofNullable((String) xPath.evaluate(pathPatternXpath, testSet, XPathConstants.STRING))
                .orElse(testSetName + "/%02d");
        NodeList tests = (NodeList) xPath.evaluate(testSetsTestsXpath, testSet, XPathConstants.NODESET);

        List<TestCaseMetaInfo> testCasesMetaInfo = new ArrayList<>();
        for (int testNumber = 0; testNumber < tests.getLength(); testNumber++) {
            NamedNodeMap test = tests.item(testNumber).getAttributes();

            boolean sample = Optional.ofNullable(test.getNamedItem(testSetsTestSampleAttribute))
                    .map(Node::getNodeValue)
                    .map(Boolean::parseBoolean)
                    .orElse(false);

            TestCaseMetaInfo.Method method = Optional.ofNullable(test.getNamedItem(testSetsTestMethodAttribute))
                    .map(Node::getNodeValue)
                    .map(TestCaseMetaInfo.Method::parse)
                    .orElse(TestCaseMetaInfo.Method.MANUAL);

            String generationCommand = Optional.ofNullable(test.getNamedItem(testSetsTestCmdAttribute))
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
        document.getDocumentElement().normalize();
        return document;
    }

}
