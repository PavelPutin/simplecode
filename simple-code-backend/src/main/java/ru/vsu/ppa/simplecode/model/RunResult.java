package ru.vsu.ppa.simplecode.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunResult {

    private int outcome;
    private String cmpinfo;
    private String stdout;
    private String stderr;
}
