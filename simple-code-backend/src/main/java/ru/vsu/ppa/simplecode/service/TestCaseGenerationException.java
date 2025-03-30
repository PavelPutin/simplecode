package ru.vsu.ppa.simplecode.service;

import lombok.Getter;
import ru.vsu.ppa.simplecode.model.RunSpec;

@Getter
public class TestCaseGenerationException extends RuntimeException {

    private final RunSpec runSpec;

    public TestCaseGenerationException(RunSpec runSpec) {
        this.runSpec = runSpec;
    }
}
