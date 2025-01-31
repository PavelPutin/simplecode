package ru.vsu.ppa.simplecode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.vsu.ppa.simplecode.model.GenerationResponse;
import ru.vsu.ppa.simplecode.model.TaskRun;
import ru.vsu.ppa.simplecode.model.Testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Log4j2
@AllArgsConstructor
public class TestGenerationService {
    private final JobeInABoxService jobeInABoxService;

    public GenerationResponse runs(TaskRun runSpec) {
        GenerationResponse result = new GenerationResponse();
        try {
            // check answer
            List<String> errors = new ArrayList<>();
            int testNumber = 1;
            for (var testcase : runSpec.getTask().getTestcases()) {
                try {
                    var stdout = jobeInABoxService.submitRun(runSpec.getAnswerLanguage(), runSpec.getTask().getAnswer(), testcase.getStdin());
                    if (!stdout.equals(testcase.getExpected())) {
                        String message = "Неправильный ответ%nОжидалось%n%s%nПолучено%n%s%n".formatted(testcase.getExpected(), stdout);
                        errors.add("Тест " + testNumber + ". " + message);
                        log.debug(errors.getLast());
                    }
                } catch (CompilationError e) {
                    errors.add("Тест " + testNumber + ". " + e.getMessage());
                    log.debug(errors.getLast());
                    break;
                } catch (RuntimeException e) {
                    errors.add("Тест " + testNumber + ". " + e.getMessage());
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
                for (int i = 0; i < generatedTestsAmount && errorsInRow <= 4; i++) {
                    try {
                        log.debug("History: {}", history.toString());
                        var stdin = jobeInABoxService.submitRun(runSpec.getTestGeneratorLanguage(), runSpec.getTask().getTestGenerator().getCustomCode(), history.toString());
                        history.add(stdin);
                        var expected = jobeInABoxService.submitRun(runSpec.getAnswerLanguage(), runSpec.getTask().getAnswer(), stdin);
                        testcases.add(new Testcase(stdin, expected));
                        errorsInRow = 0;
                    } catch (CompilationError e) {
                        errors.add("Тест " + testNumber + ". " + e.getMessage());
                        log.debug(errors.getLast());
                        break;
                    } catch (RuntimeException e) {
                        errors.add("Тест " + testNumber + ". " + e.getMessage());
                        log.debug(errors.getLast());
                        errorsInRow++;
                    } finally {
                        if (errorsInRow > 4) {
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
}
