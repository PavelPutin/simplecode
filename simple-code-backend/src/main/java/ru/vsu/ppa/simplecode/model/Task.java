package ru.vsu.ppa.simplecode.model;

import lombok.Data;

import java.util.List;

@Data
public class Task {

    private String name;
    private String questionText;
    private String defaultGrade;
    private String answer;
    private List<Testcase> testcases;
    private TestGenerator testGenerator;
}
