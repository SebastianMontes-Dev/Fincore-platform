package com.fincore.platform.infrastructure.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.mail.javamail.*;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}") private String host;
    @Value("${spring.mail.port}") private int port;
    @Value("${spring.mail.username}") private String username;
    @Value("${spring.mail.password}") private String password;
    @Value("${spring.mail.protocol}") private String protocol;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host); sender.setPort(port);
        sender.setUsername(username); sender.setPassword(password);
        sender.setProtocol(protocol);
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }
}
