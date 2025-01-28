package ru.vsu.ppa.simplecode.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ru.vsu.ppa.simplecode.model.Task;

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
@AllArgsConstructor
public class PolygonConverterService {

    private final DocumentBuilder xmlDocumentBuilder;
    private final XPath xPath;

    private record TaskMetaInfo(String name, int timeLimit, int memoryList, Path solutionSource, String language) {
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
        }
        return null;
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
     * @throws PolygonPackageIncomplete if the zip file does not contain a problem.xml file
     * @throws PolygonProblemXMLIncomplete if the problem.xml file is incomplete or missing required elements
     */
    @SneakyThrows()
    private TaskMetaInfo extractTaskMetaInfo(ZipFile zip) {
        val problemXmlDescription = zip.getEntry("problem.xml");
        if (problemXmlDescription == null) {
            throw new PolygonPackageIncomplete("No problem.xml entry in the zip file");
        }

        val document = getDocument(zip, problemXmlDescription);

        val taskNameElement = (Node) xPath.evaluate("/problem/names/name", document, XPathConstants.NODE);
        val taskName = Optional.ofNullable(taskNameElement)
                .map(element -> element.getAttributes().getNamedItem("value"))
                .map(Node::getNodeValue)
                .orElse("Unknown");

        val timeLimitMillisElement = (Double) xPath.evaluate("/problem/judging/testset/time-limit", document, XPathConstants.NUMBER);
        val timeLimitMillis = Optional.ofNullable(timeLimitMillisElement)
                .map(Double::intValue)
                .orElse(1000);

        val memoryLimitElement = (Double) xPath.evaluate("/problem/judging/testset/memory-limit", document, XPathConstants.NUMBER);
        val memoryLimit = Optional.ofNullable(memoryLimitElement)
                .map(Double::intValue)
                .orElse(268435456); // 256 MB

        val solutionSourceElement = (Node) xPath.evaluate("/problem/assets/solutions/solution[@tag='main']/source", document, XPathConstants.NODE);
        if (solutionSourceElement == null) {
            throw new PolygonProblemXMLIncomplete("Not found: " + "/problem/assets/solutions/solution[@tag='main']/source");
        }

        val solutionSource = Optional.of(solutionSourceElement)
                .map(element -> element.getAttributes().getNamedItem("path"))
                .map(Node::getNodeValue)
                .map(Paths::get)
                .orElseThrow(() -> new PolygonProblemXMLIncomplete("Not found: " + "/problem/assets/solutions/solution[@tag='main']/source[@path]"));
        val language = Optional.of(solutionSourceElement)
                .map(element -> element.getAttributes().getNamedItem("type"))
                .map(Node::getNodeValue)
                .orElseThrow(() -> new PolygonProblemXMLIncomplete("Not found: " + "/problem/assets/solutions/solution[@tag='main']/source[@type]"));

        // Return a new TaskMetaInfo object with the extracted task name and default values for other fields
        return new TaskMetaInfo(taskName, timeLimitMillis, memoryLimit, solutionSource, language);
    }

    private Document getDocument(ZipFile zip, ZipEntry problemXmlDescription) throws SAXException, IOException {
        val document = xmlDocumentBuilder.parse(zip.getInputStream(problemXmlDescription));
        document.getDocumentElement().normalize();
        return document;
    }
}
