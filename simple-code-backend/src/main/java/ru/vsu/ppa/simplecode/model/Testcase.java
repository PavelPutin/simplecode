package ru.vsu.ppa.simplecode.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Testcase {

    private final String stdin;
    private final String expected;
    private boolean display;
}
