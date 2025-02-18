package ru.vsu.ppa.simplecode.controller;

import com.google.common.primitives.Ints;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.ppa.simplecode.model.TaskRun;
import ru.vsu.ppa.simplecode.service.TestGenerationService;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class TasksRpcController {

    private final TestGenerationService testGenerationService;

    @PostMapping("/runs")
    public ResponseEntity<?> submitRun(@RequestBody TaskRun taskRun) {
        if (Ints.tryParse(taskRun.getGeneratedTestsAmount()) == null) {
            return ResponseEntity.badRequest()
                    .body("Количество тестов должно быть целым числом");
        }
        var result = testGenerationService.runs(taskRun);
        return ResponseEntity.ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(result);
    }
}
