package ru.vsu.ppa.simplecode.model;

import java.util.List;
import java.util.Map;

public record ProgramingProblem(String name,
                                int timeLimitMillis,
                                long memoryLimitMB,
                                String statement,
                                ProgramSourceCode mainSolution,
                                Map<String, ProgramSourceCode> generators,
                                List<PolygonTestcase> testCases) {}
