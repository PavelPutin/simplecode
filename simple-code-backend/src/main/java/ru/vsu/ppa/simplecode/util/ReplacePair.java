package ru.vsu.ppa.simplecode.util;

import java.util.regex.Pattern;

public record ReplacePair(Pattern pattern, String replacement) {}