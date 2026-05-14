package cn.lilicould.liliblog.common.util;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.Arrays;

/**
 * Markdown 工具类
 */
public class MarkdownUtil {

    /**
     * 创建一个 Markdown 解析器
     */
    public static Parser markdownParser() {
        return Parser.builder(commonOptions()).build();
    }

    /**
     * 创建一个 Markdown HTML 渲染器
     */
    public static HtmlRenderer markdownHtmlRenderer() {
        return HtmlRenderer.builder(commonOptions()).build();
    }

    /**
     * 将 Markdown 转换为 HTML
     */
    public static String markdownToHtml(String markdown) {

        Parser markdownParser = markdownParser();
        HtmlRenderer htmlRenderer = markdownHtmlRenderer();

        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Node document = markdownParser.parse(markdown);
        return htmlRenderer.render(document);
    }

    /**
     * 共享的配置选项
     */
    private static MutableDataSet commonOptions() {
        MutableDataSet options = new MutableDataSet();

        // ========== 1. 启用常用扩展 ==========
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),           // 表格支持
                StrikethroughExtension.create(),    // 删除线 ~~text~~
                AutolinkExtension.create(),         // 自动链接（裸URL变成超链接）
                TaskListExtension.create(),         // 任务列表 - [ ] / - [x]
                TocExtension.create()               // 目录生成 [TOC]（可选）
        ));

        // ========== 2. 安全设置（重要） ==========
        // 禁止 Markdown 中内嵌的原始 HTML 标签被解析，防止 XSS 攻击
        options.set(HtmlRenderer.SUPPRESS_HTML, true);

        // 对 URL 进行百分号编码，避免特殊字符导致的问题
        options.set(HtmlRenderer.PERCENT_ENCODE_URLS, true);

        // ========== 3. 输出美化 ==========
        // 设置 HTML 缩进大小为 2 个空格（默认是 0）
        options.set(HtmlRenderer.INDENT_SIZE, 2);

        // 可选：将软回车（\n）渲染为 <br>（默认是空格）
        options.set(HtmlRenderer.SOFT_BREAK, "<br>\n");

        return options;
    }
}
