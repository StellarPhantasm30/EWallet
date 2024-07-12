package com.project.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class EmailConfig {

    @Bean
    SimpleMailMessage getMailMessage() {
        return new SimpleMailMessage();
    }

    @Bean
    JavaMailSenderImpl getMailSender() throws IOException {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        Properties prop = new Properties();
        InputStream inputStream = getClass().getResourceAsStream("notification.config");
        prop.load(inputStream);

        javaMailSender.setHost(prop.getProperty("notification.email.host"));
        javaMailSender.setPort(Integer.parseInt(prop.getProperty("notification.email.port")));
        javaMailSender.setUsername(prop.getProperty("notification.email.username"));
        javaMailSender.setPassword(prop.getProperty("notification.email.password"));

        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.debug", true);

        properties.put("mail.smtp.starttls.enable", true);

        return javaMailSender;
    }
}