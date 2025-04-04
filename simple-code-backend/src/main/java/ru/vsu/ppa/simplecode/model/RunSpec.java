package ru.vsu.ppa.simplecode.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("run_spec")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public record RunSpec(
        @JsonProperty("language_id") String languageId,
        @JsonProperty("sourcecode") String sourceCode,
        String input,
        @JsonProperty("file_list")
        List<List<String>> files,
        Parameters parameters) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Parameters(
            @JsonProperty("runargs") List<String> runArguments,
            @JsonProperty("compileargs") List<String> compileArgs) {}
}
