package ru.vsu.ppa.simplecode.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
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
import ru.vsu.ppa.simplecode.configuration.JobeResponses;
import ru.vsu.ppa.simplecode.model.RunResult;
import ru.vsu.ppa.simplecode.model.RunSpec;

@Service
@AllArgsConstructor
@Log4j2
public class JobeInABoxService {

    private final JobeResponses jobeResponses;
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
        if (runResult.getOutcome() == jobeResponses.ok()) {
            return runResult.getStdout()
                    .stripTrailing();
        }

        if (runResult.getOutcome() == jobeResponses.compilationError()) {
            val e = new CompilationError("Ошибка компиляции: " + runResult.getCmpinfo());
            log.debug(e.getMessage());
            throw e;
        }

        String message = getExceptionMessage(runResult);
        log.debug(message);
        throw new RuntimeException(message);
    }

    private String getExceptionMessage(RunResult runResult) {
        String message;
        if (runResult.getOutcome() == jobeResponses.runtimeError()) {
            message = "Ошибка выполнения: " + runResult.getStderr();
        } else if (runResult.getOutcome() == jobeResponses.timeLimitExceeded()) {
            message = "Превышено время ожидания";
        } else if (runResult.getOutcome() == jobeResponses.memoryLimitExceeded()) {
            message = "Превышено ограничение по памяти";
        } else if (runResult.getOutcome() == jobeResponses.illegalSystemCall()) {
            message = "Запрещённый системный вызов";
        } else if (runResult.getOutcome() == jobeResponses.internalError()) {
            message = "Ошибка сервера";
        } else if (runResult.getOutcome() == jobeResponses.serverOverload()) {
            message = "Сервер перегружен";
        } else {
            throw new IllegalStateException("Unexpected value: " + runResult.getOutcome());
        }
        return message;
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
