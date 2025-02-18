package ru.vsu.ppa.simplecode.model;

import java.util.List;
import lombok.Data;

@Data
public class Task {

    private String name;
    private String questionText;
    private String defaultGrade;
    private String answer;
    private List<Testcase> testcases;
    private TestGenerator testGenerator;
}
