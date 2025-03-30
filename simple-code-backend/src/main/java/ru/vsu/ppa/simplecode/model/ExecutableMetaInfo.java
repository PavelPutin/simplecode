package ru.vsu.ppa.simplecode.model;

import java.nio.file.Path;

public record ExecutableMetaInfo(Path path, SourceCodeLanguage language) {}
