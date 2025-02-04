package ru.vsu.ppa.simplecode.service;

import java.text.MessageFormat;

public class PolygonProblemXMLIncomplete extends RuntimeException {

    public PolygonProblemXMLIncomplete(String s) {
        super(s);
    }

    public static PolygonProblemXMLIncomplete tagNotFound(String tag) {
        return new PolygonProblemXMLIncomplete(MessageFormat.format("Not found: {0}", tag));
    }

    public static PolygonProblemXMLIncomplete tagWithAttributeNotFound(String tag, String attribute) {
        return new PolygonProblemXMLIncomplete(MessageFormat.format("Not found: {0}[@{1}]", tag, attribute));
    }
}
