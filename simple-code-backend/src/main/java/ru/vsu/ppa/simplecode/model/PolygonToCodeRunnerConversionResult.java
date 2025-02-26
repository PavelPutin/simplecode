package ru.vsu.ppa.simplecode.model;

import java.util.List;

public record PolygonToCodeRunnerConversionResult(
        ProgramingProblem problem,
        List<RunSpec> stdinGenerationErrors,
        List<RunSpec> expectedGenerationErrors
) {
}
