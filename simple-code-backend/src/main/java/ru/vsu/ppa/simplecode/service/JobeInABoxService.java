package ru.vsu.ppa.simplecode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.vsu.ppa.simplecode.model.RunResult;
import ru.vsu.ppa.simplecode.model.RunSpec;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
@Log4j2
public class JobeInABoxService {

    private final RestClient jobeRestClient;

    public String submitRun(RunSpec runSpec)
            throws ExecutionException, InterruptedException, JsonProcessingException {
        val runResult = jobeRestClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(runSpec)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::clientErrorsHandler)
                .onStatus(HttpStatusCode::is5xxServerError, this::unknownErrorsHandler)
                .body(RunResult.class);

        if (runResult == null) {
            throw new RuntimeException("Empty response");
        }
        if (runResult.getOutcome() == 15) {
            return runResult.getStdout()
                    .stripTrailing();
        }

        if (runResult.getOutcome() == 11) {
            val e = new CompilationError("Ошибка компиляции: " + runResult.getCmpinfo());
            log.debug(e.getMessage());
            throw e;
        }

        val message = switch (runResult.getOutcome()) {
            case 12 -> "Ошибка выполнения: " + runResult.getStderr();
            case 13 -> "Превышено время ожидания";
            case 17 -> "Превышено ограничение по памяти";
            case 19 -> "Запрещённый системный вызов";
            case 20 -> "Ошибка сервера";
            case 21 -> "Сервер перегружен";
            default -> throw new IllegalStateException("Unexpected value: " + runResult.getOutcome());
        };
        log.debug(message);
        throw new RuntimeException(message);
    }

    private void unknownErrorsHandler(
            HttpRequest request,
            ClientHttpResponse response) throws IOException {
        val message = "Ошибка сервера: неизвестный http статус ответа: " + response.getStatusCode();
        log.debug(message);
        throw new RuntimeException(message);
    }

    private void clientErrorsHandler(
            HttpRequest request,
            ClientHttpResponse response) throws IOException {
        val message = new String(response.getBody()
                                         .readAllBytes());
        log.debug("Error {}", message);
        throw new RuntimeException(message);
    }

}
