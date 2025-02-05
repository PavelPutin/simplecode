package ru.vsu.ppa.simplecode.model;

import lombok.Data;

@Data
public class TaskRun {

    private final String answerLanguage;
    private final String testGeneratorLanguage;
    private final String generatedTestsAmount;
    private final Task task;
}
