package ru.vsu.ppa.simplecode.model;

import java.util.List;

public record GenerationResponse(List<Testcase> testcases, List<String> errors) {

}
