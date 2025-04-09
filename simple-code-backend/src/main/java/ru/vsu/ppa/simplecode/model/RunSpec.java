package ru.vsu.ppa.simplecode.model;

import java.util.Arrays;
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
        @JsonProperty("file_list") List<List<String>> files,
        Parameters parameters) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Parameters(
            @JsonProperty("runargs") List<String> runArguments,
            @JsonProperty("compileargs") List<String> compileArgs) {}

    public interface LanguageIdSetter {

        SourceCodeSetter languageId(String languageId);
    }

    public interface SourceCodeSetter {

        OptionalSetter sourceCode(String sourceCode);
    }

    public interface OptionalSetter {

        OptionalSetter input(String input);

        OptionalSetter files(JobeRunAssetFile... file);

        OptionalSetter runArgs(String... runArg);

        OptionalSetter compileArgs(String... compileArg);

        RunSpec build();
    }

    public static LanguageIdSetter builder() {
        return new Builder();
    }

    public static class Builder implements LanguageIdSetter, SourceCodeSetter, OptionalSetter {

        private String languageId;
        private String sourceCode;
        private String input;
        private List<List<String>> files;
        private List<String> runArguments;
        private List<String> compileArgs;

        @Override
        public SourceCodeSetter languageId(String languageId) {
            this.languageId = languageId;
            return this;
        }

        @Override
        public OptionalSetter sourceCode(String sourceCode) {
            this.sourceCode = sourceCode;
            return this;
        }

        @Override
        public OptionalSetter input(String input) {
            this.input = input;
            return this;
        }

        @Override
        public OptionalSetter files(JobeRunAssetFile... file) {
            this.files = Arrays.stream(file).map(JobeRunAssetFile::asList).toList();
            return this;
        }

        @Override
        public OptionalSetter runArgs(String... runArg) {
            this.runArguments = Arrays.asList(runArg);
            return this;
        }

        @Override
        public OptionalSetter compileArgs(String... compileArg) {
            this.compileArgs = Arrays.asList(compileArg);
            return this;
        }

        @Override
        public RunSpec build() {
            return new RunSpec(
                    languageId,
                    sourceCode,
                    input,
                    files,
                    new Parameters(runArguments, compileArgs)
            );
        }
    }
}
