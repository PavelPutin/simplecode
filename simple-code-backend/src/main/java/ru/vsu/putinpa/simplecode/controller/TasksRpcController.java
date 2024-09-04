package ru.vsu.putinpa.simplecode.controller;

import com.google.common.primitives.Ints;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.putinpa.simplecode.model.TaskRun;
import ru.vsu.putinpa.simplecode.service.JobeInABoxService;


@CrossOrigin
@RestController
@RequiredArgsConstructor
public class TasksRpcController {

    private final JobeInABoxService jobeService;

    @PostMapping("/runs")
    public ResponseEntity<?> submitRun(@RequestBody TaskRun taskRun) {
        if (Ints.tryParse(taskRun.getGeneratedTestsAmount()) == null) {
            return ResponseEntity.badRequest().body("Количество тестов должно быть целым числом");
        }
        var result = jobeService.runs(taskRun);
        return ResponseEntity.ok().header("Content-Type", "application/json; charset=UTF-8").body(result);
    }
}