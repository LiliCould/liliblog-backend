package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.util.MailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * EmailTemplateService测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailTemplateServiceTest {

    @Mock
    private MailUtil mailUtil;

    private EmailTemplateService emailTemplateService;

    @BeforeEach
    void setUp() {
        emailTemplateService = new EmailTemplateService(mailUtil);
    }

    private String captureHtml() {
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtil).sendHtmlMail(anyString(), anyString(), htmlCaptor.capture());
        return htmlCaptor.getValue();
    }

    @Test
    void sendVerificationCodeEmailReplacesVariables() {
        emailTemplateService.sendVerificationCodeEmail("user@example.com", "123456");

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtil).sendHtmlMail(anyString(), subjectCaptor.capture(), htmlCaptor.capture());
        assertEquals("立里博客登录验证码", subjectCaptor.getValue());
        String html = htmlCaptor.getValue();
        assertTrue(html.contains("123456"));
        assertTrue(html.contains("您正在使用验证码登录立里博客。"));
        assertFalse(html.contains("${code}"));
    }

    @Test
    void sendArticleReviewResultApproved() {
        emailTemplateService.sendArticleReviewResult("author@example.com", "我的文章", true, "通过");

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtil).sendHtmlMail(anyString(), subjectCaptor.capture(), htmlCaptor.capture());
        assertEquals("文章审核通过通知", subjectCaptor.getValue());
        assertTrue(htmlCaptor.getValue().contains("审核通过"));
        assertTrue(htmlCaptor.getValue().contains("我的文章"));
    }

    @Test
    void sendArticleReviewResultRejectedWithReason() {
        emailTemplateService.sendArticleReviewResult("author@example.com", "我的文章", false, "内容违规");

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtil).sendHtmlMail(anyString(), subjectCaptor.capture(), htmlCaptor.capture());
        assertEquals("文章审核未通过通知", subjectCaptor.getValue());
        assertTrue(htmlCaptor.getValue().contains("审核未通过"));
        assertTrue(htmlCaptor.getValue().contains("内容违规"));
    }

    @Test
    void sendArticleReviewResultRejectedWithoutReason() {
        emailTemplateService.sendArticleReviewResult("author@example.com", "我的文章", false, null);

        assertTrue(captureHtml().contains("未提供具体原因"));
    }

    @Test
    void sendArticleReviewNotifyReplacesVariables() {
        emailTemplateService.sendArticleReviewNotify("admin@example.com", "新文章", "张三", "2026-05-25 10:00:00");

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtil).sendHtmlMail(anyString(), subjectCaptor.capture(), htmlCaptor.capture());
        assertEquals("【待审核】新文章提交通知", subjectCaptor.getValue());
        String html = htmlCaptor.getValue();
        assertTrue(html.contains("新文章"));
        assertTrue(html.contains("张三"));
        assertTrue(html.contains("2026-05-25 10:00:00"));
    }

    @Test
    void sendCommentReviewNotifyReplacesPendingCount() {
        emailTemplateService.sendCommentReviewNotify("admin@example.com", 3);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailUtil).sendHtmlMail(anyString(), subjectCaptor.capture(), htmlCaptor.capture());
        assertEquals("【提醒】待审核评论数量提醒", subjectCaptor.getValue());
        assertTrue(htmlCaptor.getValue().contains("3"));
    }

    @Test
    void sendTemplateMailWhenTemplateNotFoundThrowsRuntimeException() {
        assertThrows(RuntimeException.class,
                () -> emailTemplateService.sendTemplateMail("admin@example.com", "主题",
                        "templates/not-exist.html", java.util.Map.of("k", "v")));
    }

    @Test
    void sendTemplateMailWhenMailFailsThrowsRuntimeException() {
        doThrow(new RuntimeException("smtp down"))
                .when(mailUtil).sendHtmlMail(anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class,
                () -> emailTemplateService.sendTemplateMail("admin@example.com", "主题",
                        "templates/verification-code-email.html", java.util.Map.of("code", "123456")));
    }
}