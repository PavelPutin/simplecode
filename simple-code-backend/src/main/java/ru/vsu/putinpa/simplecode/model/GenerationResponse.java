package ru.vsu.putinpa.simplecode.model;

import lombok.Data;

import java.util.List;

@Data
public class GenerationResponse {
    private List<Testcase> testcases;
    private List<String> errors;
}
