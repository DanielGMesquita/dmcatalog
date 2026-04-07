package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.services.exceptions.EmailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Value("${spring.mail.username}")
  private String fromEmail;

  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendEmail(String to, String subject, String body) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);
    } catch (MailException e) {
      throw new EmailException("Failed to send email: " + e.getMessage());
    }
  }
}
