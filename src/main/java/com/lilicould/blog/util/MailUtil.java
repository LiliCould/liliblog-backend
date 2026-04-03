package com.lilicould.blog.util;


import com.lilicould.blog.entity.MailBean;
import com.lilicould.blog.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 发送邮件
 */
@Component
@RequiredArgsConstructor
public class MailUtil {
    /**
     * 邮件发送者
     */
    @Value("${spring.mail.username}")
    private String MAIL_SENDER;

    /**
     * 注入 QQ 发送邮件的 bean
     */
    @Resource
    private final JavaMailSender javaMailSender;

    public void sendMail(String mail,String content) {
        MailBean mailBean = new MailBean();
        //接收者
        mailBean.setRecipient(mail);
        //标题
        mailBean.setSubject("立里博客注册验证码");
        //内容主体
        mailBean.setContent(content);
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(MAIL_SENDER);
            helper.setTo(mailBean.getRecipient());
            helper.setSubject(mailBean.getSubject());

            // HTML 格式的邮件内容，类似 Steam 风格
            String htmlContent = String.format(
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; padding: 40px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                ".header { text-align: center; color: #1b2838; margin-bottom: 30px; }" +
                ".code-container { background-color: #66c0f9; color: #ffffff; padding: 30px; border-radius: 6px; text-align: center; margin: 30px 0; }" +
                ".verification-code { font-size: 48px; font-weight: bold; letter-spacing: 8px; color: #ffffff; }" +
                ".content { color: #5c7d8a; line-height: 1.6; margin-top: 30px; }" +
                ".footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #e0e0e0; color: #888; font-size: 12px; text-align: center; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1 class='header'>立里博客</h1>" +
                "<div class='code-container'>" +
                "<div style='font-size: 16px; margin-bottom: 15px;'>您的验证码是：</div>" +
                "<div class='verification-code'>%s</div>" +
                "</div>" +
                "<div class='content'>" +
                "<p>您好！</p>" +
                "<p>您正在申请立里博客账号验证，请在验证码输入框中输入上面的验证码以完成身份验证。</p>" +
                "<p>如非本人操作，请忽略此邮件。</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>此邮件为系统自动发送，请勿回复</p>" +
                "<p>&copy; 2025 立里博客</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>",
                content
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("邮件发送失败：" + e.getMessage());
        }
    }
}
