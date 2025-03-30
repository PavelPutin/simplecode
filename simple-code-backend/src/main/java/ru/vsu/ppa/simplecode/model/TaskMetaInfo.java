package ru.vsu.ppa.simplecode.model;

import java.nio.file.Path;
import java.util.List;
import org.springframework.util.unit.DataSize;

public record TaskMetaInfo(String name,
                           int timeLimit,
                           DataSize memoryLimit,
                           Path statementPath,
                           ExecutableMetaInfo mainSolution,
                           List<ExecutableMetaInfo> generators,
                           List<TestCaseMetaInfo> testCases) {}
