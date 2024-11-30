package ru.vsu.putinpa.simplecode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.vsu.putinpa.simplecode.model.GenerationResponse;
import ru.vsu.putinpa.simplecode.model.RunResult;
import ru.vsu.putinpa.simplecode.model.TaskRun;
import ru.vsu.putinpa.simplecode.model.Testcase;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class JobeInABoxService {
    public GenerationResponse runs(TaskRun runSpec) {
        GenerationResponse result = new GenerationResponse();
        try (var client = HttpClient.newHttpClient()) {

            // check answer
            List<String> errors = new ArrayList<>();
            int testNumber = 1;
            for (var testcase : runSpec.getTask().getTestcases()) {
                try {
                    var stdout = submitRun(client, runSpec.getAnswerLanguage(), runSpec.getTask().getAnswer(), Optional.of(testcase.getStdin()));
                    if (!stdout.equals(testcase.getExpected())) {
                        String message = "Неправильный ответ%nОжидалось%n%s%nПолучено%n%s%n".formatted(testcase.getExpected(), stdout);
                        errors.add("Тест " + testNumber + ". " + message);
                    }
                } catch (CompilationError e) {
                    errors.add("Тест " + testNumber + ". " + e.getMessage());
                    break;
                } catch (RuntimeException e) {
                    errors.add("Тест " + testNumber + ". " + e.getMessage());
                } finally {
                    testNumber++;
                }
            }

            // generate tests
            List<Testcase> testcases = new ArrayList<>();
            if (errors.isEmpty()) {
                List<String> history = new ArrayList<>();
                int errorsInRow = 0;
                for (int i = 0; i < Integer.parseInt(runSpec.getGeneratedTestsAmount()) && errorsInRow <= 4; i++) {
                    try {
                        System.out.println(history);
                        var stdin = submitRun(client, runSpec.getTestGeneratorLanguage(), runSpec.getTask().getTestGenerator().getCustomCode(), Optional.of(history.toString()));
                        history.add(stdin);
                        var expected = submitRun(client, runSpec.getAnswerLanguage(), runSpec.getTask().getAnswer(), Optional.of(stdin));
                        testcases.add(new Testcase(stdin, expected));
                        errorsInRow = 0;
                    }  catch (CompilationError e) {
                        errors.add("Тест " + testNumber + ". " + e.getMessage());
                        break;
                    } catch (RuntimeException e) {
                        errors.add("Тест " + testNumber + ". " + e.getMessage());
                        errorsInRow++;
                    } finally {
                        if (errorsInRow > 4) {
                            errors.add("Генерация тестов прервана: 5 ошибок подряд");
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

    private String submitRun(HttpClient client, String languageId, String sourceCode, Optional<String> stdin) throws ExecutionException, InterruptedException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        var runSpec = objectMapper.createObjectNode()
                .put("language_id", languageId)
                .put("sourcecode", sourceCode);
        stdin.ifPresent(s -> runSpec.put("input", s));
        var root = objectMapper.createObjectNode();
        root.set("run_spec", runSpec);

        var body = objectMapper.writeValueAsString(root);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://jobe/jobe/index.php/restapi/runs"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset-utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        var response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
        String message;
        switch (response.statusCode()) {
            case 200:
                var mapper = new ObjectMapper();
                var runResult = mapper.readValue(response.body(), RunResult.class);
                switch (runResult.getOutcome()) {
                    case 11:
                        message = "Ошибка компиляции: %s".formatted(runResult.getCmpinfo());
                        throw new CompilationError(message);
                    case 12:
                        message = "Ошибка выполнения: %s".formatted(runResult.getStderr());
                        throw new RuntimeException(message);
                    case 13:
                        message = "Превышено время ожидания";
                        throw new RuntimeException(message);
                    case 15:
                        return runResult.getStdout().stripTrailing();
                    case 17:
                        message = "Превышено ограничение по памяти";
                        throw new RuntimeException(message);
                    case 19:
                        message = "Запрещённый системный вызов";
                        throw new RuntimeException(message);
                    case 20:
                        message = "Ошибка сервера";
                        throw new RuntimeException(message);
                    case 21:
                        message = "Сервер перегружен";
                        throw new RuntimeException(message);
                }
                break;
            case 202:

                break;
            case 400, 404:
                message = response.body();
                System.out.println("{"+
                        "\"run_spec\": {"+
                        "\"language_id\": \"" + languageId + "\","+
                        "\"sourcecode\": \"" + sourceCode + "\"" +
                        (stdin.map(s -> ", \"input\": \"" + s + "\"").orElse("")) +
                        "}" +
                        "}");
                throw new RuntimeException(message);
            default:
                message = "Ошибка сервера: неизвестный http статус ответа";
                throw new RuntimeException(message);
        }
        return "";
    }

    private static class CompilationError extends RuntimeException {
        public CompilationError(String message) {
            super(message);
        }
    }
}
