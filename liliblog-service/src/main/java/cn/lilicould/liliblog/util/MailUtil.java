package cn.lilicould.liliblog.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * 邮件发送工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailUtil {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送纯文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件内容
     */
    public void sendTextMail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail, "立里博客");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);
            log.info("纯文本邮件发送成功，收件人: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("纯文本邮件发送失败，收件人: {}", to, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 发送HTML格式邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param html    HTML内容
     */
    public void sendHtmlMail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "立里博客");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("HTML邮件发送成功，收件人: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("HTML邮件发送失败，收件人: {}", to, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 发送带附件的HTML邮件
     *
     * @param to          收件人邮箱
     * @param subject     邮件主题
     * @param html        HTML内容
     * @param attachmentPath 附件路径（classpath路径）
     * @param attachmentName 附件名称
     */
    public void sendHtmlMailWithAttachment(String to, String subject, String html,
                                           String attachmentPath, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "立里博客");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            ClassPathResource resource = new ClassPathResource(attachmentPath);
            helper.addAttachment(attachmentName, resource);

            mailSender.send(message);
            log.info("带附件HTML邮件发送成功，收件人: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("带附件HTML邮件发送失败，收件人: {}", to, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 发送带内嵌图片的HTML邮件
     *
     * @param to       收件人邮箱
     * @param subject  邮件主题
     * @param html     HTML内容（使用cid:引用图片）
     * @param imageId  图片ID（在HTML中通过cid:imageId引用）
     * @param imagePath 图片路径（classpath路径）
     */
    public void sendHtmlMailWithInlineImage(String to, String subject, String html,
                                            String imageId, String imagePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "立里博客");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            ClassPathResource image = new ClassPathResource(imagePath);
            helper.addInline(imageId, image);

            mailSender.send(message);
            log.info("带内嵌图片HTML邮件发送成功，收件人: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("带内嵌图片HTML邮件发送失败，收件人: {}", to, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}
