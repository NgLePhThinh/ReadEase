package com.ReadEase.ReadEase.Service;

import com.ReadEase.ReadEase.Utils.EmailUtils;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
@Service
@RequiredArgsConstructor
public class EmailService {
    public static final String UTF_8_ENCODING = "UTF-8";
    public static final String EMAIL_TEMPLATE = "emailtemplate";
    public static final String TEXT_HTML_ENCONDING = "text/html";
    public static final String RESET_YOUR_READ_EASE_PASSWORD = "Reset your ReadEase Password";
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;
    private String clientHost  = "https://read-ease.azurewebsites.net";
    private final TemplateEngine templateEngine;
    private final JavaMailSender emailSender;

    public void sendSimpleEmail(String name, String to, String token){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(RESET_YOUR_READ_EASE_PASSWORD);
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setText(EmailUtils.getEmailMessage(name,host, token));
            emailSender.send(message);

        }catch (Exception e){
            System.out.println(e.getMessage());
            throw  new RuntimeException(e.getMessage());
        }
    }
    @Async
    public void sendHTMLEmail(String to, String token){
        try{
            Context context = new Context();
            context.setVariables(Map.of("url",EmailUtils.getVerificationUrl(clientHost,token)));
            String text = templateEngine.process(EMAIL_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject(RESET_YOUR_READ_EASE_PASSWORD);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setText(text, true);

            emailSender.send(message);
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw  new RuntimeException(e.getMessage());
        }

    }
    private MimeMessage getMimeMessage() {
        return emailSender.createMimeMessage();
    }


}
