package ru.vsu.ppa.simplecode.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.ppa.simplecode.model.ExceptionDto;
import ru.vsu.ppa.simplecode.model.PolygonConvertSpecificationDto;
import ru.vsu.ppa.simplecode.model.PolygonToCodeRunnerConversionResult;
import ru.vsu.ppa.simplecode.service.PolygonConverterService;
import ru.vsu.ppa.simplecode.util.PolygonPackageIncomplete;
import ru.vsu.ppa.simplecode.util.PolygonProblemXMLIncomplete;

@RestController()
@RequestMapping("/v1/polygon-converter")
@AllArgsConstructor
public class PolygonConverterController {

    private final PolygonConverterService polygonConverterService;

    @PostMapping()
    public PolygonToCodeRunnerConversionResult convertPolygonPackageToProgrammingProblem(
            @RequestParam(value = "testSizeConstraint", required = false) Integer testSizeConstraint,
            @RequestParam("package") MultipartFile polygonPackage) {
        System.out.println("!!! test size constraint " + testSizeConstraint);
        var convertSpecificationDto = new PolygonConvertSpecificationDto(testSizeConstraint);
        return polygonConverterService.convertPolygonPackageToProgrammingProblem(polygonPackage, convertSpecificationDto);
    }

    @ExceptionHandler(PolygonPackageIncomplete.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleException(PolygonPackageIncomplete e) {
        return new ExceptionDto(1, e.getMessage());
    }

    @ExceptionHandler(PolygonProblemXMLIncomplete.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleException(PolygonProblemXMLIncomplete e) {
        return new ExceptionDto(2, e.getMessage());
    }
}
