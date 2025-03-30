package ru.vsu.ppa.simplecode.model;

import lombok.val;
import ru.vsu.ppa.simplecode.util.TexToHtmlConverter;

public record Statement(String legend, String input, String output, String notes) {

    public String getAsHtml() {
        val legend = TexToHtmlConverter.convert(this.legend);
        val input = TexToHtmlConverter.convert(this.input);
        val output = TexToHtmlConverter.convert(this.output);
        val notes = TexToHtmlConverter.convert(this.notes);
        return legend + "<h3>Входные данные</h3>" + input + "<h3>Выходные данные</h3>"
                + output + "<h3>Примечания</h3>" + notes;
    }
}