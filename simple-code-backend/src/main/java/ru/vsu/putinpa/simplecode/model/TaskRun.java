package ru.vsu.putinpa.simplecode.model;

import lombok.Data;

import java.util.List;

@Data
public class TaskRun {
    private final String answerLanguage;
    private final String testGeneratorLanguage;
    private final String generatedTestsAmount;
    private final Task task;
}
