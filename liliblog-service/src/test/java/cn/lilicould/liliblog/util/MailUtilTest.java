package cn.lilicould.liliblog.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MailUtil邮件工具测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
class MailUtilTest {

    @Mock
    private JavaMailSender mailSender;

    private MailUtil mailUtil;

    @BeforeEach
    void setUp() {
        mailUtil = new MailUtil(mailSender);
        ReflectionTestUtils.setField(mailUtil, "fromEmail", "no-reply@lilicould.cn");
    }

    @Test
    void sendTextMailSuccess() {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        mailUtil.sendTextMail("user@example.com", "主题", "内容");

        verify(mailSender).send(message);
    }

    @Test
    void sendTextMailFailureThrowsRuntimeException() throws MessagingException {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);
        doThrow(new MessagingException("连接失败"))
                .when(message).setRecipient(any(Message.RecipientType.class), any(InternetAddress.class));

        assertThrows(RuntimeException.class,
                () -> mailUtil.sendTextMail("user@example.com", "主题", "内容"));
    }

    @Test
    void sendHtmlMailSuccess() {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        mailUtil.sendHtmlMail("user@example.com", "主题", "<h1>内容</h1>");

        verify(mailSender).send(message);
    }

    @Test
    void sendHtmlMailFailureThrowsRuntimeException() throws MessagingException {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);
        doThrow(new MessagingException("服务器异常"))
                .when(message).setRecipient(any(Message.RecipientType.class), any(InternetAddress.class));

        assertThrows(RuntimeException.class,
                () -> mailUtil.sendHtmlMail("user@example.com", "主题", "<h1>内容</h1>"));
    }

    @Test
    void sendHtmlMailWithAttachmentSuccess() {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        mailUtil.sendHtmlMailWithAttachment("user@example.com", "主题", "<p>内容</p>", "static/report.pdf", "report.pdf");

        verify(mailSender).send(message);
    }

    @Test
    void sendHtmlMailWithInlineImageSuccess() {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        mailUtil.sendHtmlMailWithInlineImage("user@example.com", "主题", "<img src='cid:logo'>", "logo", "static/logo.png");

        verify(mailSender).send(message);
    }

    @Test
    void sendWithAttachmentFailureThrowsRuntimeException() throws MessagingException {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);
        doThrow(new MessagingException("fail"))
                .when(message).setRecipient(any(Message.RecipientType.class), any(InternetAddress.class));

        assertThrows(RuntimeException.class,
                () -> mailUtil.sendHtmlMailWithAttachment("user@example.com", "主题", "内容", "static/a.txt", "a.txt"));
        assertThrows(RuntimeException.class,
                () -> mailUtil.sendHtmlMailWithInlineImage("user@example.com", "主题", "内容", "img", "static/a.png"));
    }
}