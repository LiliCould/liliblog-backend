package util;

import com.lilicould.blog.util.MarkdownUtil;
import org.junit.Test;

public class MarkdownTest {
    @Test
    public void markdownToPlainTextTest() {
        String markdown = "### 标题\n" +
                "- 列表1\n" +
                "- 列表2\n" +
                "- 列表3\n" +
                "- 列表4\n" +
                "- 列表5\n" +
                "- 列表6\n" +
                "- 列表7\n" +
                "- 列表8\n" +
                "- 列表9\n" +
                "- 列表10\n";
        String plainText = MarkdownUtil.markdownToPlainText(markdown);
        System.out.println(plainText);

    }
}
