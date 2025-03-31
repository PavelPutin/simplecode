package ru.vsu.ppa.simplecode.model;

import lombok.val;
import ru.vsu.ppa.simplecode.util.TexToHtmlConverter;

public record Statement(String legend, String input, String output, String notes) {

    public String getAsHtml() {
        val legendHtml = TexToHtmlConverter.convert(this.legend);
        val inputHtml = TexToHtmlConverter.convert(this.input);
        val outputHtml = TexToHtmlConverter.convert(this.output);
        val notesHtml = TexToHtmlConverter.convert(this.notes);
        var result = legendHtml;
        if (!inputHtml.isEmpty()) {
            result += "<h3>Входные данные</h3>" + inputHtml;
        }
        if (!outputHtml.isEmpty()) {
            result += "<h3>Выходные данные</h3>" + outputHtml;
        }
        if (!notesHtml.isEmpty()) {
            result += "<h3>Примечания</h3>" + notesHtml;
        }
        return result;
    }
}
