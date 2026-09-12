package cn.lilicould.liliblog.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MarkdownUtil Markdown转换测试类
 *
 * @author lilicould
 */
class MarkdownUtilTest {

    @Test
    void markdownToHtmlConvertsHeading() {
        String html = MarkdownUtil.markdownToHtml("# 标题");

        assertTrue(html.contains("<h1"));
        assertTrue(html.contains("标题"));
    }

    @Test
    void markdownToHtmlConvertsTable() {
        String html = MarkdownUtil.markdownToHtml("| a | b |\n|---|---|\n| 1 | 2 |");

        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<td>1</td>"));
    }

    @Test
    void markdownToHtmlConvertsStrikethrough() {
        String html = MarkdownUtil.markdownToHtml("~~删除~~");

        assertTrue(html.contains("<del>"));
    }

    @Test
    void markdownToHtmlConvertsTaskList() {
        String html = MarkdownUtil.markdownToHtml("- [x] 完成\n- [ ] 未完成");

        assertTrue(html.contains("checkbox"));
    }

    @Test
    void markdownToHtmlConvertsBareUrlToLink() {
        String html = MarkdownUtil.markdownToHtml("访问 https://example.com 查看");

        assertTrue(html.contains("<a href="));
    }

    @Test
    void markdownToHtmlSuppressesRawHtmlForXss() {
        String html = MarkdownUtil.markdownToHtml("<script>alert('xss')</script>");

        assertFalse(html.contains("<script>"));
    }

    @Test
    void markdownToHtmlReturnsEmptyForBlank() {
        assertEquals("", MarkdownUtil.markdownToHtml(null));
        assertEquals("", MarkdownUtil.markdownToHtml(""));
        assertEquals("", MarkdownUtil.markdownToHtml("   "));
    }

    @Test
    void markdownParserAndRendererBuildable() {
        assertNotNull(MarkdownUtil.markdownParser());
        assertNotNull(MarkdownUtil.markdownHtmlRenderer());
        assertNotNull(MarkdownUtil.markdownParser().parse("hello"));
    }
}