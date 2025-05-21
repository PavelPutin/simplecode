package ru.vsu.ppa.simplecode.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
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
import ru.vsu.ppa.simplecode.model.JobeRunAssetFile;
import ru.vsu.ppa.simplecode.model.RunResult;
import ru.vsu.ppa.simplecode.model.RunSpec;

@Service
@AllArgsConstructor
@Log4j2
public class JobeInABoxService {

    private final RestClient jobeRestClient;
    private final Semaphore jobeRunsSemaphore;

    /**
     * Submits a run specification to the Jobe REST client and returns the standard output of the run.
     *
     * @param runSpec the run specification to submit
     * @return the standard output of the run
     * @throws ExecutionException      if an error occurs during execution
     * @throws InterruptedException    if the thread is interrupted
     * @throws JsonProcessingException if an error occurs during JSON processing
     */
    public String submitRun(RunSpec runSpec) throws ExecutionException, InterruptedException, JsonProcessingException {
        jobeRunsSemaphore.acquire();
        val runResult = jobeRestClient.post().uri("/runs").contentType(MediaType.APPLICATION_JSON)
                .body(runSpec)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::clientErrorsHandler)
                .onStatus(HttpStatusCode::is5xxServerError, this::unknownErrorsHandler).body(RunResult.class);
        jobeRunsSemaphore.release();

        if (runResult == null) {
            throw new RuntimeException("Empty response");
        }

        val jobeResponse = JobeResponses.fromCode(runResult.outcome());
        if (jobeResponse.equals(JobeResponses.OK)) {
            return runResult.stdout().stripTrailing();
        }

        if (jobeResponse.equals(JobeResponses.COMPILATION_ERROR)) {
            val e = new CompilationError("Ошибка компиляции: " + runResult.cmpinfo());
            log.debug(e.getMessage());
            throw e;
        }

        String message = getExceptionMessage(runResult, jobeResponse);
        log.debug(message);
        throw new RuntimeException(message);
    }

    private String getExceptionMessage(RunResult runResult, JobeResponses jobeResponse) {
        return switch (jobeResponse) {
            case RUNTIME_ERROR -> "Ошибка выполнения: " + runResult.stderr();
            case TIME_LIMIT_EXCEEDED -> "Превышено время ожидания";
            case MEMORY_LIMIT_EXCEEDED -> "Превышено ограничение по памяти";
            case ILLEGAL_SYSTEM_CALL -> "Запрещённый системный вызов";
            case INTERNAL_ERROR -> "Ошибка сервера";
            case SERVER_OVERLOAD -> "Сервер перегружен";
            default -> throw new IllegalStateException("Unexpected value: " + jobeResponse);
        };
    }

    private void unknownErrorsHandler(HttpRequest request, ClientHttpResponse response) throws IOException {
        val message = "Ошибка сервера: неизвестный http статус ответа: " + response.getStatusCode();
        log.debug(message);
        throw new RuntimeException(message);
    }

    private void clientErrorsHandler(HttpRequest request, ClientHttpResponse response) throws IOException {
        val message = new String(response.getBody().readAllBytes());
        log.debug("Error {}", message);
        if (response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))) {
            throw new JobeFileNotFoundException(message);
        }
        throw new RuntimeException(message);
    }

    public void putFile(JobeRunAssetFile file) {
        jobeRestClient.put()
                .uri("/files/{id}", Map.of("id", file.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("file_contents", file.value()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::clientErrorsHandler)
                .onStatus(HttpStatusCode::is5xxServerError, this::unknownErrorsHandler)
                .toBodilessEntity();
    }

}
