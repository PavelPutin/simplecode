package ru.vsu.ppa.simplecode.model;

import java.util.List;

public record JobeRunAssetFile(String id, String name, boolean source, String value) {
    public List<String> asList() {
        return List.of(id, name, Boolean.toString(source));
    }
}
