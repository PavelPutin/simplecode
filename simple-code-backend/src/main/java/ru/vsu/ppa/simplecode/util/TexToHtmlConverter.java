package ru.vsu.ppa.simplecode.util;

import java.util.List;
import java.util.regex.Pattern;

public class TexToHtmlConverter {
    private static final List<ReplacePair> REPLACE_PAIRS = List.of(
            new ReplacePair(Pattern.compile("\\r\\n", Pattern.DOTALL), "\n"),
            new ReplacePair(Pattern.compile("~---", Pattern.DOTALL), " &mdash;"),
            new ReplacePair(Pattern.compile("[`']", Pattern.DOTALL), "'"),
            new ReplacePair(Pattern.compile("\\${1,2}(.*?)\\${1,2}", Pattern.DOTALL), "\\\\($1\\\\)"),
            new ReplacePair(Pattern.compile("<<(.*?)>>"), "&laquo;$1&raquo;"),
            new ReplacePair(Pattern.compile("\\\\(bf|textbf)\\{(.+?)}", Pattern.DOTALL), "<strong>$2</strong>"),
            new ReplacePair(Pattern.compile("\\\\(it|textit)\\{(.+?)}", Pattern.DOTALL), "<em>$2</em>"),
            new ReplacePair(Pattern.compile("\\\\(t|tt|texttt)\\{(.+?)}", Pattern.DOTALL), "<code>$2</code>"),
            new ReplacePair(Pattern.compile("\\\\(tiny|scriptsize|small|normalsize|large|Large|LARGE|huge|Huge)\\{(.+?)}", Pattern.DOTALL), "$1"),
            new ReplacePair(Pattern.compile("\\\\url\\{(.+?)}", Pattern.DOTALL), "<a href=\"$1\">$1</a>"),
            new ReplacePair(Pattern.compile("\\\\href\\{(.+?)}\\{(.+?)}", Pattern.DOTALL), "<a href=\"$1\">$2</a>"),
            new ReplacePair(Pattern.compile("\\\\(emph|underline)\\{(.+?)}", Pattern.DOTALL),
                            "<span style=\"text-decoration: underline;\">$2</span>"),
            new ReplacePair(Pattern.compile("\\\\textsc\\{(.+?)}", Pattern.DOTALL),
                            "<span style=\"text-transform: capitalize;\">$1</span>"),
            new ReplacePair(Pattern.compile("\\\\sout\\{(.+?)}", Pattern.DOTALL), "<s>$1</s>"),
            new ReplacePair(Pattern.compile("(?!=\\n{2})(\\\\begin\\{enumerate}.*?\\\\end\\{enumerate}|\\\\begin\\{itemize}.*?\\\\end\\{itemize}|\\\\begin\\{lstlisting}(.*?)\\\\end\\{lstlisting})", Pattern.DOTALL),
                            "\n\n$1"),
            new ReplacePair(Pattern.compile("\\n{3,}", Pattern.DOTALL), "\n\n"),
            new ReplacePair(Pattern.compile("(.+?)(\\n{2}|$)", Pattern.DOTALL), "<p>$1</p>"),
            new ReplacePair(Pattern.compile("<p>\\\\begin\\{center}(.*?)\\\\end\\{center}</p>", Pattern.DOTALL),
                            "<p style=\"text-align: center;\">$1</p>"),
            new ReplacePair(Pattern.compile("\\\\begin\\{center}(.*?)\\\\end\\{center}", Pattern.DOTALL),
                            "<p style=\"text-align: center;\">$1</p>"),
            new ReplacePair(Pattern.compile("<p>\\\\begin\\{lstlisting}(.*?)\\\\end\\{lstlisting}</p>", Pattern.DOTALL),
                            "<pre><code>$1</code></pre>"),
            new ReplacePair(Pattern.compile("\\\\begin\\{lstlisting}(.*?)\\\\end\\{lstlisting}", Pattern.DOTALL),
                            "<pre><code>$1</code></pre>"),
            new ReplacePair(Pattern.compile("<p>\\\\begin\\{itemize}(.*?)\\\\end\\{itemize}</p>", Pattern.DOTALL),
                            "<ul>$1</ul>"),
            new ReplacePair(Pattern.compile("\\\\begin\\{itemize}(.*?)\\\\end\\{itemize}", Pattern.DOTALL),
                            "<ul>$1</ul>"),
            new ReplacePair(Pattern.compile("<p>\\\\begin\\{enumerate}(.*?)\\\\end\\{enumerate}</p>", Pattern.DOTALL),
                            "<ol>$1</ol>"),
            new ReplacePair(Pattern.compile("\\\\begin\\{enumerate}(.*?)\\\\end\\{enumerate}", Pattern.DOTALL),
                           "<ol>$1</ol>"),
            new ReplacePair(Pattern.compile("\\\\item (.+?)\n", Pattern.DOTALL),
                            "<li>$1</li>"),
            new ReplacePair(Pattern.compile("\\\\includegraphics.*?\\{(.*?)}", Pattern.DOTALL),
                            "<img class=\"img-fluid align-top\" style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"@@PLUGINFILE@@/$1\" alt=\"$1\"/>")
    );

    public static String convert(String text) {
        for (ReplacePair replacePair : REPLACE_PAIRS) {
            text = replacePair.pattern().matcher(text).replaceAll(replacePair.replacement());
        }
        return text;
    }
}
