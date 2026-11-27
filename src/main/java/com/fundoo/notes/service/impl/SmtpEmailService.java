package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Sends plain text emails through the SMTP server configured with the
 * spring.mail.* properties.
 *
 * @author Navya Raj
 */
@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    // e.g. "5 Jan 2030, 18:30"
    private static final DateTimeFormatter REMINDER_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailService(JavaMailSender mailSender, @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendOtpEmail(String to, String firstName, String otp, long validMinutes) {
        send(to, "Fundoo Notes - your password reset OTP", "Hi " + firstName + ",\n\n"
                + "Your one time password to reset your Fundoo Notes password is:\n\n"
                + "    " + otp + "\n\n"
                + "It is valid for " + validMinutes + " minutes. If you did not ask for a password reset, you can ignore this email.\n\n"
                + "Fundoo Notes", "OTP email");
    }

    @Override
    public void sendReminderEmail(ReminderEvent event) {
        StringBuilder text = new StringBuilder()
                .append("Hi ").append(event.getUserFirstName()).append(",\n\n")
                .append("This is your reminder for the note \"").append(event.getNoteTitle()).append("\"")
                .append(" set for ").append(event.getReminderTime().format(REMINDER_FORMAT)).append(".\n");
        if (event.getMessage() != null && !event.getMessage().isBlank()) {
            text.append("\nMessage: ").append(event.getMessage()).append("\n");
        }
        if (event.getNoteDescription() != null && !event.getNoteDescription().isBlank()) {
            text.append("\nNote:\n").append(event.getNoteDescription()).append("\n");
        }
        text.append("\nFundoo Notes");
        send(event.getUserEmail(), "Fundoo Notes reminder: " + event.getNoteTitle(), text.toString(), "Reminder email");
    }

    // one place that builds, sends and logs every email
    private void send(String to, String subject, String text, String kind) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
            // the email body (which can contain an OTP) is never written to the log
            log.info("{} sent to {}", kind, to);
        } catch (MailException e) {
            log.error("Could not send {} to {}: {}", kind, to, e.getMessage());
            throw new EmailDeliveryException("Could not send the " + kind + ", please try again later", e);
        }
    }
}
