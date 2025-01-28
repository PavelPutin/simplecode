package ru.vsu.ppa.simplecode.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Node;
import ru.vsu.putinpa.simplecode.model.Task;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

@Service
@Log4j2
public class PolygonConverterService {

    private record TaskMetaInfo(String name, int timeLimit, int memoryList, Path solutionSource, String language) {}

    /**
     * Converts a polygon package to a programming problem.
     *
     * @param polygonPackage the multipart file representing the polygon package
     * @return the converted programming problem
     * @throws IOException if an I/O error occurs while converting the polygon package
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
     * @throws IOException if an I/O error occurs while resolving the multipart file
     */
    @SneakyThrows(IOException.class)
    private ZipFile multipartResolver(MultipartFile polygonPackage) {
        val tmpdir = Files.createTempDirectory(null).toString();
        val zipPath = Paths.get(tmpdir, polygonPackage.getOriginalFilename());
        polygonPackage.transferTo(zipPath);
        return new ZipFile(zipPath.toFile());
    }

    /**
     * Extracts task meta information from a given zip file.
     *
     * @param zip the zip file to extract task meta information from
     * @return the extracted task meta information
     * @throws Exception if an error occurs while parsing the zip file or extracting task meta information
     */
    @SneakyThrows()
    private TaskMetaInfo extractTaskMetaInfo(ZipFile zip) {
        // Get the problem.xml entry from the zip file
        // TODO: check for null value
        val problemXmlDescription = zip.getEntry("problem.xml");

        // Create a new document builder factory and builder
        val factory = DocumentBuilderFactory.newInstance();
        val builder = factory.newDocumentBuilder();

        // Parse the problem.xml entry from the zip file
        // TODO: check for exceptions
        val document = builder.parse(zip.getInputStream(problemXmlDescription));
        document.getDocumentElement().normalize();

        // Create a new XPath instance
        XPath xPath = XPathFactory.newInstance().newXPath();

        // Evaluate the XPath expression to get the task name elements
        val taskNameElement = (Node) xPath.evaluate("/problem/names/name", document, XPathConstants.NODE);

        // Get the value of the first task name element
        // TODO: check for null value
        val taskName = taskNameElement.getAttributes().getNamedItem("value").getNodeValue();

        val timeLimitElement = (Double) xPath.evaluate("/problem/judging/testset/time-limit", document, XPathConstants.NUMBER);
        val timeLimit = timeLimitElement.intValue();

        val memoryLimitElement = (Double) xPath.evaluate("/problem/judging/testset/memory-limit", document, XPathConstants.NUMBER);
        val memoryLimit = memoryLimitElement.intValue();

        val solutionSourceElement = (Node) xPath.evaluate("/problem/assets/solutions/solution[@tag='main']/source", document, XPathConstants.NODE);
        val solutionSource = Paths.get(solutionSourceElement.getAttributes().getNamedItem("path").getNodeValue());

        val languageElement = (Node) xPath.evaluate("/problem/assets/solutions/solution[@tag='main']/source", document, XPathConstants.NODE);
        val language = languageElement.getAttributes().getNamedItem("type").getNodeValue();

        // Return a new TaskMetaInfo object with the extracted task name and default values for other fields
        return new TaskMetaInfo(taskName, timeLimit, memoryLimit, solutionSource, language);
    }
}
