package ru.vsu.ppa.simplecode.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RunResult(int outcome, String cmpinfo, String stdout, String stderr) {

}
