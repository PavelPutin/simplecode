package ru.vsu.ppa.simplecode.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PolygonTestcase extends Testcase {

    @ToString.Exclude
    @JsonIgnore
    public final TestCaseMetaInfo metaInfo;
    public boolean display;

    public PolygonTestcase(TestCaseMetaInfo metaInfo) {
        super(null, null);
        this.display = metaInfo.sample();
        this.metaInfo = metaInfo;
    }
}
