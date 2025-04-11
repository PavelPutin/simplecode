package ru.vsu.ppa.simplecode.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class PolygonTestcase extends Testcase {

    @ToString.Exclude
    @JsonIgnore
    private final TestCaseMetaInfo metaInfo;
    @ToString.Exclude
    @JsonIgnore
    @Setter
    private RunSpec stdinGenerationError;
    @ToString.Exclude
    @JsonIgnore
    @Setter
    private RunSpec expectedGenerationError;
    private final boolean display;

    public PolygonTestcase(TestCaseMetaInfo metaInfo) {
        super(null, null);
        this.display = metaInfo.sample();
        this.metaInfo = metaInfo;
    }
}
