package com.example.tourism_rear.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EmailVerificationService {

    private static final int CODE_EXPIRE_MINUTES = 10;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailVerificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegisterCode(String email, String username) {
        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (!StringUtils.hasText(fromAddress)) {
            throw new RuntimeException("邮件服务未配置，请先设置 MAIL_USERNAME / MAIL_PASSWORD 等环境变量");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES);
        codeStore.put(email, new CodeEntry(code, expireAt));

        String displayName = StringUtils.hasText(username) ? username : email;
        String htmlContent = buildHtmlContent(displayName, code);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("验证你的账户");
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("发送邮箱验证码失败: {}", e.getMessage(), e);
            throw new RuntimeException("验证码发送失败，请稍后再试");
        }
    }

    public void verifyRegisterCode(String email, String code) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            throw new RuntimeException("邮箱或验证码不能为空");
        }
        CodeEntry entry = codeStore.get(email);
        if (entry == null) {
            throw new RuntimeException("验证码已失效，请重新获取");
        }
        if (entry.expireAt.isBefore(LocalDateTime.now())) {
            codeStore.remove(email);
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!entry.code.equals(code)) {
            throw new RuntimeException("验证码不正确");
        }
        codeStore.remove(email);
    }

    private String buildHtmlContent(String userName, String code) {
        String safeName = escapeHtml(userName);
        String template =
                "<!doctype html>"
                        + "<html lang=\"zh-CN\">"
                        + "<head>"
                        + "<meta charset=\"UTF-8\">"
                        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                        + "<title>验证你的账户</title>"
                        + "</head>"
                        + "<body style=\"margin:0;padding:0;background:#f5f7f6;font-family:'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;\">"
                        + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding:24px 12px;\">"
                        + "<tr><td align=\"center\">"
                        + "<table role=\"presentation\" width=\"620\" cellpadding=\"0\" cellspacing=\"0\" "
                        + "style=\"background:#ffffff;border-radius:16px;box-shadow:0 12px 30px rgba(31,56,94,0.12);padding:28px;\">"
                        + "<tr><td style=\"font-size:20px;font-weight:700;color:#1f2b2a;\">验证你的账户</td></tr>"
                        + "<tr><td style=\"padding-top:12px;font-size:14px;color:#5b6a65;line-height:1.7;\">"
                        + "{{NAME}} 用户，你好：<br>"
                        + "您正在尝试注册中山文旅账户。请输入这组验证码："
                        + "</td></tr>"
                        + "<tr><td style=\"padding:18px 0;\">"
                        + "<div style=\"font-size:32px;font-weight:700;letter-spacing:6px;color:#1f7a6d;text-align:center;"
                        + "background:#f0f6f4;border-radius:12px;padding:14px 0;\">"
                        + "{{CODE}}"
                        + "</div></td></tr>"
                        + "<tr><td style=\"font-size:13px;color:#7a857f;line-height:1.7;\">"
                        + "切勿与任何人分享这组验证码。<br><br>"
                        + "如果有人索要这组验证码，请勿透露，尤其是对方声称是相关工作人员时，可能企图入侵你的账户。<br><br>"
                        + "未请求获得这组验证码？如果你收到这封邮件，但并未尝试验证账户，请忽略此邮件。"
                        + "</td></tr>"
                        + "<tr><td style=\"padding-top:20px;font-size:12px;color:#9aa5a0;\">谢谢！<br>中山文旅 安全团队</td></tr>"
                        + "</table></td></tr></table></body></html>";
        return template.replace("{{NAME}}", safeName).replace("{{CODE}}", code);
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private record CodeEntry(String code, LocalDateTime expireAt) {}
}
