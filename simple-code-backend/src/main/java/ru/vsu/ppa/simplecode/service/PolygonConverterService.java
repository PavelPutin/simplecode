package ru.vsu.ppa.simplecode.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.ppa.simplecode.model.PolygonTestcase;
import ru.vsu.ppa.simplecode.model.PolygonToCodeRunnerConversionResult;
import ru.vsu.ppa.simplecode.model.ProgramSourceCode;
import ru.vsu.ppa.simplecode.model.ProgramingProblem;
import ru.vsu.ppa.simplecode.model.RunSpec;
import ru.vsu.ppa.simplecode.model.StatementFile;
import ru.vsu.ppa.simplecode.model.TestCaseMetaInfo;
import ru.vsu.ppa.simplecode.util.PolygonZipAccessObject;

@Log4j2
@RequiredArgsConstructor
public abstract class PolygonConverterService {

    private final JobeInABoxService jobeInABoxService;

    /**
     * Converts a polygon package to a programming problem.
     *
     * @param polygonPackage the multipart file representing the polygon package
     * @return the converted programming problem
     */
    @SneakyThrows(IOException.class)
    public PolygonToCodeRunnerConversionResult convertPolygonPackageToProgrammingProblem(MultipartFile polygonPackage) {
        try (ZipFile zip = multipartResolver(polygonPackage)) {
            val polygonZipAccessObject = getPolygonZipAccessObject(zip);
            return getPolygonToCodeRunnerConversionResult(polygonZipAccessObject);
        }
    }

    protected abstract PolygonZipAccessObject getPolygonZipAccessObject(ZipFile zip);

    @SneakyThrows
    private PolygonToCodeRunnerConversionResult getPolygonToCodeRunnerConversionResult(
            PolygonZipAccessObject polygonZipAccessObject) {
        val statement = polygonZipAccessObject.extractStatement();
        log.debug("Statement: {}", statement);

        List<StatementFile> images = polygonZipAccessObject.extractImagesFromStatement(statement);
        log.debug("Images: {}", images.stream().map(StatementFile::name).toList());

        val mainSolution = polygonZipAccessObject.extractMainSolution();
        log.debug("Main solution: {}", mainSolution);

        Map<String, ProgramSourceCode> generators = polygonZipAccessObject.extractGenerators();
        log.debug("Generators: {}", generators);

        List<PolygonTestcase> testCases = polygonZipAccessObject.extractTestCases();

        List<RunSpec> stdinGenerationErrors = new ArrayList<>();
        List<RunSpec> expectedGenerationErrors = new ArrayList<>();

        testCases.forEach(testCase -> {
            log.debug("Test case: {}/{}",
                      testCase.getMetaInfo().testSetName(),
                      testCase.getMetaInfo().number() + 1);

            try {
                String stdin = polygonZipAccessObject.extractStdin(testCase)
                        .orElseGet(() -> generateStdin(testCase, generators));
                testCase.setStdin(stdin);
            } catch (TestCaseGenerationException e) {
                stdinGenerationErrors.add(e.getRunSpec());
                testCase.setExpected(null);
            }

            try {
                String expected = polygonZipAccessObject.extractExpected(testCase)
                        .orElseGet(() -> generateExpected(testCase, mainSolution));
                testCase.setExpected(expected);
            } catch (TestCaseGenerationException e) {
                expectedGenerationErrors.add(e.getRunSpec());
                testCase.setExpected(null);
            }
        });

        log.debug("Test cases ({}):", testCases.size());
        testCases.forEach(testCase -> log.debug("Test case: {}", testCase));

        log.debug("Stdin generation errors ({}):", stdinGenerationErrors.size());
        stdinGenerationErrors.forEach(log::debug);

        log.debug("Expected generation errors ({}):", expectedGenerationErrors.size());
        expectedGenerationErrors.forEach(log::debug);

        val problem = new ProgramingProblem(
                polygonZipAccessObject.extractName(),
                polygonZipAccessObject.extractTimeLimit(),
                polygonZipAccessObject.extractMemoryLimit(),
                statement.getAsHtml(),
                images,
                mainSolution,
                generators,
                testCases
        );
        log.debug("Problem statement HTML: {}", problem.statement());
        return new PolygonToCodeRunnerConversionResult(problem, stdinGenerationErrors, expectedGenerationErrors);
    }

    private String generateStdin(PolygonTestcase testCase,
                                 Map<String, ProgramSourceCode> generators) {
        if (testCase.getMetaInfo().method() != TestCaseMetaInfo.Method.GENERATED) {
            return null;
        }
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
            val result = jobeInABoxService.submitRun(runSpec);
            log.debug("Generate stdin value: {}", result);
            return result;
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            throw new TestCaseGenerationException(runSpec);
        }
    }

    private String generateExpected(PolygonTestcase testCase, ProgramSourceCode mainSolution) {
        if (testCase.getStdin() == null) {
            return null;
        }
        val runSpec = new RunSpec(mainSolution.language().getJobeNotation(),
                                  mainSolution.content(),
                                  testCase.getStdin(),
                                  null);
        try {
            val result = jobeInABoxService.submitRun(runSpec);
            log.debug("Generate expected value: {}", result);
            return result;
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            throw new TestCaseGenerationException(runSpec);
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
}
