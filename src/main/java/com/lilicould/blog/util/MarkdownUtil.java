package com.lilicould.blog.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

public class MarkdownUtil {

    // 将Markdown转换为纯文本
    public static String markdownToPlainText(String markdown) {
        Parser parser = Parser.builder().build();
        TextContentRenderer renderer = TextContentRenderer.builder().build();
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    // 转义HTML字符
    public static String escapeHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        StringBuilder sb = new StringBuilder(html.length());
        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
