package ru.vsu.putinpa.simplecode.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunResult {
    private int outcome;
    private String cmpinfo;
    private String stdout;
    private String stderr;
}
