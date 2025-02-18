package ru.vsu.ppa.simplecode.model;

import java.util.List;
import lombok.Data;

@Data
public class GenerationResponse {

    private List<Testcase> testcases;
    private List<String> errors;
}
