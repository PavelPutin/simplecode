package ru.vsu.ppa.simplecode.model;

import java.util.List;

public record Task(String name,
                   String questionText,
                   String defaultGrade,
                   String answer,
                   List<Testcase> testcases,
                   TestGenerator testGenerator) {

}
