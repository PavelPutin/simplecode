package ru.vsu.ppa.simplecode.controller;

import com.google.common.primitives.Ints;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.ppa.simplecode.model.GenerationResponse;
import ru.vsu.ppa.simplecode.model.TaskRun;
import ru.vsu.ppa.simplecode.service.TestGenerationService;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class TasksRpcController {

    private final TestGenerationService testGenerationService;

    @PostMapping("/runs")
    public GenerationResponse submitRun(@RequestBody TaskRun taskRun) {
        if (Ints.tryParse(taskRun.getGeneratedTestsAmount()) == null) {
            throw new IllegalArgumentException("Количество тестов должно быть целым числом");
        }
        return testGenerationService.runs(taskRun);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e) {
        return e.getMessage();
    }
}
