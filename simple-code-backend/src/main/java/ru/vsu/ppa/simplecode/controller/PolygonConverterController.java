package ru.vsu.ppa.simplecode.controller;

import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.ppa.simplecode.model.Task;
import ru.vsu.ppa.simplecode.service.PolygonConverterService;

@RestController()
@RequestMapping("/v1/polygon-converter")
@AllArgsConstructor
public class PolygonConverterController {

    private final PolygonConverterService polygonConverterService;

    @PostMapping()
    public ResponseEntity<Task> convertPolygonPackageToProgrammingProblem(
            @RequestParam("package") MultipartFile polygonPackage) {
        val task = polygonConverterService.convertPolygonPackageToProgrammingProblem(polygonPackage);
        return ResponseEntity.ok(task);
    }
}
