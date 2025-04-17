package ru.vsu.ppa.simplecode.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.ZipFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.ppa.simplecode.model.JobeRunAssetFile;
import ru.vsu.ppa.simplecode.model.PolygonTestcase;
import ru.vsu.ppa.simplecode.model.PolygonToCodeRunnerConversionResult;
import ru.vsu.ppa.simplecode.model.ProgramSourceCode;
import ru.vsu.ppa.simplecode.model.ProgramingProblem;
import ru.vsu.ppa.simplecode.model.RunSpec;
import ru.vsu.ppa.simplecode.model.StatementFile;
import ru.vsu.ppa.simplecode.model.TestCaseMetaInfo;
import ru.vsu.ppa.simplecode.util.PolygonZipAccessObject;
import ru.vsu.ppa.simplecode.util.PolygonZipAccessObjectProvider;

@Log4j2
@Service
@RequiredArgsConstructor
public class PolygonConverterService {

    private final PolygonZipAccessObjectProvider polygonZipAccessObjectProvider;
    private final JobeInABoxService jobeInABoxService;
    private final String base64TestLibHeaderFile;
    private final JobeRunAssetFile testLibHeaderFile;

    /**
     * Converts a polygon package to a programming problem.
     *
     * @param polygonPackage the multipart file representing the polygon package
     * @return the converted programming problem
     */
    @SneakyThrows(IOException.class)
    public PolygonToCodeRunnerConversionResult convertPolygonPackageToProgrammingProblem(MultipartFile polygonPackage) {
        try (ZipFile zip = multipartResolver(polygonPackage)) {
            val polygonZipAccessObject = polygonZipAccessObjectProvider.getZipAccessObject(zip);
            return getPolygonToCodeRunnerConversionResult(polygonZipAccessObject);
        }
    }

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

        try (ForkJoinPool threadPool = new ForkJoinPool(8)) {
            threadPool.execute(() -> {
                testCases.parallelStream().forEach(testCase -> {
                    log.debug("Test case: {}/{}",
                              testCase.getMetaInfo().testSetName(),
                              testCase.getMetaInfo().number() + 1);

                    try {
                        String stdin = polygonZipAccessObject.extractStdin(testCase)
                                .orElseGet(() -> generateStdin(testCase, generators));
                        testCase.setStdin(stdin);
                    } catch (TestCaseGenerationException e) {
                        testCase.setStdinGenerationError(e.getRunSpec());
                        testCase.setExpected(null);
                    }

                    try {
                        String expected = polygonZipAccessObject.extractExpected(testCase)
                                .orElseGet(() -> generateExpected(testCase, mainSolution));
                        testCase.setExpected(expected);
                    } catch (TestCaseGenerationException e) {
                        testCase.setExpectedGenerationError(e.getRunSpec());
                        testCase.setExpected(null);
                    }
                });
            });
        }

        List<RunSpec> stdinGenerationErrors = testCases.stream()
                .map(PolygonTestcase::getStdinGenerationError)
                .filter(Objects::nonNull)
                .toList();
        List<RunSpec> expectedGenerationErrors = testCases.stream()
                .map(PolygonTestcase::getExpectedGenerationError)
                .filter(Objects::nonNull)
                .toList();

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
        List<String> compileArgs = List.of("-w");
        val runSpec = RunSpec.builder()
                .languageId(generator.language().getJobeNotation())
                .sourceCode(generator.content())
                .files(testLibHeaderFile)
                .runArgs(args.toArray(new String[0]))
                .compileArgs("-w")
                .build();
        try {
            String result;
            try {
                result = jobeInABoxService.submitRun(runSpec);
            } catch (JobeFileNotFoundException e) {
                jobeInABoxService.putFile(testLibHeaderFile, base64TestLibHeaderFile);
                log.debug("Put file (stdin generation) {}", testLibHeaderFile);
                result = jobeInABoxService.submitRun(runSpec);
            }
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
        val runSpec = RunSpec.builder()
                .languageId(mainSolution.language().getJobeNotation())
                .sourceCode(mainSolution.content())
                .input(testCase.getStdin())
                .files(testLibHeaderFile)
                .compileArgs("-w")
                .build();
        try {
            String result;
            try {
                result = jobeInABoxService.submitRun(runSpec);
            } catch (JobeFileNotFoundException e) {
                jobeInABoxService.putFile(testLibHeaderFile, base64TestLibHeaderFile);
                log.debug("Put file (expected generation) {}", testLibHeaderFile);
                result = jobeInABoxService.submitRun(runSpec);
            }
            log.debug("Generate expected value: {}", result);
            return result;
        }  catch (ExecutionException | InterruptedException | JsonProcessingException e) {
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
