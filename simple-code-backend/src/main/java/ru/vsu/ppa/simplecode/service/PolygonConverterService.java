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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ru.vsu.ppa.simplecode.model.Task;
import ru.vsu.ppa.simplecode.util.PathHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
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

    private record TaskMetaInfo(String name, int timeLimit, DataSize memoryLimit, Path solutionSource,
                                String mainSolutionLanguage) {

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

    /**
     * Extracts task meta information from a zip file containing a problem.xml file.
     *
     * @param zip the zip file containing the problem.xml file
     * @return a TaskMetaInfo object containing the extracted task meta information
     * @throws PolygonPackageIncomplete    if the zip file does not contain a problem.xml file
     * @throws PolygonProblemXMLIncomplete if the problem.xml file is incomplete or missing required elements
     */
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

        // Return a new TaskMetaInfo object with the extracted task name and default values for other fields
        return new TaskMetaInfo(taskName, timeLimitMillis, memoryLimit, solutionSource, language);
    }

    private Document getDocument(ZipFile zip, ZipEntry problemXmlDescription) throws SAXException, IOException {
        val document = xmlDocumentBuilder.parse(zip.getInputStream(problemXmlDescription));
        document.getDocumentElement().normalize();
        return document;
    }
}
