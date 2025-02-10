package ru.vsu.ppa.simplecode.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PolygonTestcase extends Testcase {

    @ToString.Exclude
    public final TestCaseMetaInfo metaInfo;
    public boolean display;

    public PolygonTestcase(TestCaseMetaInfo metaInfo) {
        super(null, null);
        this.display = metaInfo.sample();
        this.metaInfo = metaInfo;
    }
}
