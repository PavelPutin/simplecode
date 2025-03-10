package ru.vsu.ppa.simplecode.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

@JsonTypeName("run_spec")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public record RunSpec(
        @JsonProperty("language_id") String languageId,
        @JsonProperty("sourcecode") String sourceCode,
        String input,
        Parameters parameters) {

    public record Parameters(
            @JsonProperty("runargs") List<String> runArguments) {}
}
