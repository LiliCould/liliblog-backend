package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.util.MailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 邮件模板服务, 用于发送基于模板的HTML邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final MailUtil mailUtil;

    /**
     * 发送基于模板的HTML邮件
     *
     * @param to       收件人邮箱
     * @param subject  邮件主题
     * @param templatePath 模板路径（classpath路径）
     * @param variables 模板变量
     */
    @Async
    public void sendTemplateMail(String to, String subject, String templatePath, Map<String, String> variables) {
        try {
            String htmlContent = loadTemplate(templatePath, variables);
            mailUtil.sendHtmlMail(to, subject, htmlContent);
            log.info("模板邮件发送成功，收件人: {}, 模板: {}", to, templatePath);
        } catch (Exception e) {
            log.error("模板邮件发送失败，收件人: {}, 模板: {}", to, templatePath, e);
            throw new RuntimeException("模板邮件发送失败", e);
        }
    }

    /**
     * 加载并渲染模板
     *
     * @param templatePath 模板路径
     * @param variables 变量映射
     * @return 渲染后的HTML内容
     */
    private String loadTemplate(String templatePath, Map<String, String> variables) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String template = reader.lines().collect(Collectors.joining("\n"));

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                template = template.replace("${" + entry.getKey() + "}",
                                          entry.getValue() != null ? entry.getValue() : "");
            }

            return template;
        }
    }

    /**
     * 发送验证码邮件
     */
    @Async
    public void sendVerificationCodeEmail(String to, String code) {
        Map<String, String> variables = Map.of(
                "content", "您正在使用验证码登录立里博客。",
                "code", code
        );

        sendTemplateMail(to, "立里博客登录验证码", "templates/verification-code-email.html", variables);
    }

    /**
     * 发送文章审核结果通知
     *
     * @param to           收件人邮箱（作者）
     * @param articleTitle 文章标题
     * @param isApproved   是否通过审核
     * @param reason       审核理由（不通过时必填）
     */
    @Async
    public void sendArticleReviewResult(String to, String articleTitle, boolean isApproved,
                                        String reason) {
        Map<String, String> variables = new HashMap<>();
        variables.put("articleTitle", articleTitle);
        variables.put("statusClass", isApproved ? "status-pass" : "status-reject");
        variables.put("statusText", isApproved ? "✅ 审核通过" : "❌ 审核未通过");

        if (isApproved) {
            variables.put("reasonSection",
                    "<p style='color: #4CAF50; font-weight: bold;'>恭喜！您的文章已通过审核，现已发布。</p>");
        } else {
            variables.put("reasonSection",
                    "<div class='reason-box'>" +
                            "<div class='reason-label'>未通过原因：</div>" +
                            "<div>" + (reason != null ? reason : "未提供具体原因") + "</div>" +
                            "</div>" +
                            "<p>请根据上述建议修改后重新提交审核。</p>");
        }

        sendTemplateMail(to, isApproved ? "文章审核通过通知" : "文章审核未通过通知",
                "templates/article-review-result.html", variables);
    }

    /**
     * 发送新文章待审核通知（通知站长）
     *
     * @param adminEmail   站长邮箱
     * @param articleTitle 文章标题
     * @param authorName   作者姓名
     * @param submitTime   提交时间
     */
    @Async
    public void sendArticleReviewNotify(String adminEmail, String articleTitle,
                                        String authorName, String submitTime) {
        Map<String, String> variables = Map.of(
                "articleTitle", articleTitle,
                "authorName", authorName,
                "submitTime", submitTime
        );

        sendTemplateMail(adminEmail, "【待审核】新文章提交通知",
                "templates/article-review-notify.html", variables);
    }

    /**
     * 发送评论待审核数量提醒（通知站长）
     *
     * @param adminEmail        站长邮箱
     * @param pendingCount      待审核评论数量
     */
    @Async
    public void sendCommentReviewNotify(String adminEmail, int pendingCount) {
        Map<String, String> variables = Map.of(
                "pendingCount", String.valueOf(pendingCount)
        );

        sendTemplateMail(adminEmail, "【提醒】待审核评论数量提醒",
                "templates/comment-review-notify.html", variables);
    }
}
