package com.lilicould.blog.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

public class MarkdownUtil {
    public static String markdownToPlainText(String markdown) {
        Parser parser = Parser.builder().build();
        TextContentRenderer renderer = TextContentRenderer.builder().build();
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
