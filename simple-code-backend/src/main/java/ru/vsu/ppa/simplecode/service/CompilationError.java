package ru.vsu.ppa.simplecode.service;

public class CompilationError extends RuntimeException {

    public CompilationError(String message) {
        super(message);
    }
}
