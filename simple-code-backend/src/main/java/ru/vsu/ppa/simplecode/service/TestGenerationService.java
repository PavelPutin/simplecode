package ru.vsu.ppa.simplecode.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.stereotype.Service;
import ru.vsu.ppa.simplecode.configuration.JobeClientProperties;
import ru.vsu.ppa.simplecode.model.GenerationResponse;
import ru.vsu.ppa.simplecode.model.RunSpec;
import ru.vsu.ppa.simplecode.model.TaskRun;
import ru.vsu.ppa.simplecode.model.Testcase;

@Service
@Log4j2
@AllArgsConstructor
public class TestGenerationService {

    private final JobeClientProperties jobeClientProperties;
    private final JobeInABoxService jobeInABoxService;

    public GenerationResponse runs(TaskRun runSpec) {
        GenerationResponse result = new GenerationResponse();
        try {
            // check answer
            List<String> errors = new ArrayList<>();
            int testNumber = 1;
            for (var testcase : runSpec.getTask().getTestcases()) {
                try {
                    val runSpeck = new RunSpec(runSpec.getAnswerLanguage(),
                                               runSpec.getTask().getAnswer(),
                                               testcase.getStdin(),
                                               null);
                    var stdout = jobeInABoxService.submitRun(runSpeck);
                    if (!stdout.equals(testcase.getExpected())) {
                        String message = "Неправильный ответ%nОжидалось%n%s%nПолучено%n%s%n"
                                .formatted(testcase.getExpected(), stdout);
                        errors.add(getErrorMessage(testNumber, message));
                        log.debug(errors.getLast());
                    }
                } catch (CompilationError e) {
                    errors.add(getErrorMessage(testNumber, e.getMessage()));
                    log.debug(errors.getLast());
                    break;
                } catch (RuntimeException e) {
                    errors.add(getErrorMessage(testNumber, e.getMessage()));
                    log.debug(errors.getLast());
                } finally {
                    testNumber++;
                }
            }

            // generate tests
            List<Testcase> testcases = new ArrayList<>();
            if (errors.isEmpty()) {
                List<String> history = new ArrayList<>();
                int errorsInRow = 0;
                int generatedTestsAmount = Integer.parseInt(runSpec.getGeneratedTestsAmount());
                for (int i = 0; i < generatedTestsAmount && errorsInRow < jobeClientProperties.maxErrorsInRow(); i++) {
                    try {
                        log.debug("History: {}", history.toString());
                        val stdinGenerationRun = new RunSpec(runSpec.getTestGeneratorLanguage(),
                                                             runSpec.getTask().getTestGenerator().getCustomCode(),
                                                             history.toString(),
                                                             null);
                        var stdin = jobeInABoxService.submitRun(stdinGenerationRun);
                        history.add(stdin);

                        val expectedGenerationRun = new RunSpec(runSpec.getAnswerLanguage(),
                                                                runSpec.getTask().getAnswer(),
                                                                stdin,
                                                                null);
                        var expected = jobeInABoxService.submitRun(expectedGenerationRun);
                        testcases.add(new Testcase(stdin, expected));
                        errorsInRow = 0;
                    } catch (CompilationError e) {
                        errors.add(getErrorMessage(testNumber, e.getMessage()));
                        log.debug(errors.getLast());
                        break;
                    } catch (RuntimeException e) {
                        errors.add(getErrorMessage(testNumber, e.getMessage()));
                        log.debug(errors.getLast());
                        errorsInRow++;
                    } finally {
                        if (errorsInRow >= jobeClientProperties.maxErrorsInRow()) {
                            errors.add("Генерация тестов прервана: 5 ошибок подряд");
                            log.debug(errors.getLast());
                        }
                        testNumber++;
                    }
                }
            }

            result.setTestcases(testcases);
            result.setErrors(errors);
            return result;
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getErrorMessage(int testNumber, String e) {
        return "Тест " + testNumber + ". " + e;
    }
}
