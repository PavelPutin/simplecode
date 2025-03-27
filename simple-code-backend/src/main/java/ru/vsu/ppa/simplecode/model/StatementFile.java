package ru.vsu.ppa.simplecode.model;

public record StatementFile(String name, String path, String encoding, byte[] base64Data) {}
