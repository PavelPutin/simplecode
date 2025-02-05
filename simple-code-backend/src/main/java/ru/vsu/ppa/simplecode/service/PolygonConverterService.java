package ru.vsu.ppa.simplecode.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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

    private record TaskMetaInfo(
            String name,
            int timeLimit,
            DataSize memoryLimit,
            ExecutableMetaInfo mainSolution,
            List<ExecutableMetaInfo> generators,
            List<TestCaseMetaInfo> testCases) {}

    private record ExecutableMetaInfo(
            Path path,
            String Language) {}

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
        String pathToSolution = PathHelper.toUnixString(metaInfo.mainSolution()
                                                                .path());
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
        val tmpdir = Files.createTempDirectory(null)
                .toString();
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

        val solutionSourceElement = (Node) xPath.evaluate(problemXmlParsingProperties.solutionSourceXPath(),
                                                          document,
                                                          XPathConstants.NODE);
        if (solutionSourceElement == null) {
            throw PolygonProblemXMLIncomplete.tagNotFound(problemXmlParsingProperties.solutionSourceXPath());
        }

        ExecutableMetaInfo mainSolution = extractExecutable(solutionSourceElement,
                                                            problemXmlParsingProperties.solutionSourceXPath(),
                                                            problemXmlParsingProperties.solutionSourcePathAttribute(),
                                                            problemXmlParsingProperties.solutionSourceLanguageAttribute());

        String xPathToExecutables = "problem/files/executables/executable/source";
        NodeList executables = (NodeList) xPath.evaluate(xPathToExecutables, document, XPathConstants.NODESET);

        List<ExecutableMetaInfo> executablesMetaInfo = IntStream.range(0, executables.getLength())
                .mapToObj(executables::item)
                .map(n -> extractExecutable(n,
                                            xPathToExecutables,
                                            "path",
                                            "language"))
                .toList();

        NodeList testSets = (NodeList) xPath.evaluate(problemXmlParsingProperties.testSetsXpath(),
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

    private ExecutableMetaInfo extractExecutable(Node node,
                                                 String nodeXPath,
                                                 String pathAttribute,
                                                 String languageAttribute) {
        val pathToSource = Optional.of(node)
                .map(element -> element.getAttributes()
                        .getNamedItem(pathAttribute))
                .map(Node::getNodeValue)
                .map(Paths::get)
                .orElseThrow(() -> PolygonProblemXMLIncomplete.tagWithAttributeNotFound(nodeXPath, pathAttribute));
        val language = Optional.of(node)
                .map(element -> element.getAttributes()
                        .getNamedItem(languageAttribute))
                .map(Node::getNodeValue)
                .orElseThrow(() -> PolygonProblemXMLIncomplete.tagWithAttributeNotFound(nodeXPath, languageAttribute));
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
        String testSetName = testSet.getAttributes()
                .getNamedItem(problemXmlParsingProperties.testSetsNameAttribute())
                .getNodeValue();
        String pathPattern = Optional.ofNullable((String) xPath.evaluate(problemXmlParsingProperties.pathPatternXpath(),
                                                                         testSet,
                                                                         XPathConstants.STRING))
                .orElse(testSetName + "/%02d");
        NodeList tests = (NodeList) xPath.evaluate(problemXmlParsingProperties.testSetsTestsXpath(),
                                                   testSet,
                                                   XPathConstants.NODESET);

        List<TestCaseMetaInfo> testCasesMetaInfo = new ArrayList<>();
        for (int testNumber = 0; testNumber < tests.getLength(); testNumber++) {
            NamedNodeMap test = tests.item(testNumber)
                    .getAttributes();

            boolean sample = Optional.ofNullable(test.getNamedItem(problemXmlParsingProperties.testSetsTestSampleAttribute()))
                    .map(Node::getNodeValue)
                    .map(Boolean::parseBoolean)
                    .orElse(false);

            TestCaseMetaInfo.Method method = Optional.ofNullable(test.getNamedItem(problemXmlParsingProperties.testSetsTestMethodAttribute()))
                    .map(Node::getNodeValue)
                    .map(TestCaseMetaInfo.Method::parse)
                    .orElse(TestCaseMetaInfo.Method.MANUAL);

            String generationCommand = Optional.ofNullable(test.getNamedItem(problemXmlParsingProperties.testSetsTestCmdAttribute()))
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
