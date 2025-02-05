package ru.vsu.ppa.simplecode.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Testcase {

    private String stdin;
    private String expected;
}
