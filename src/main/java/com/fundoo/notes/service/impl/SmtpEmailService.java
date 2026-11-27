package com.fundoo.notes.service.impl;

import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends plain text emails through the SMTP server configured with the
 * spring.mail.* properties.
 *
 * @author Navya Raj
 */
@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailService(JavaMailSender mailSender, @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendOtpEmail(String to, String firstName, String otp, long validMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Fundoo Notes - your password reset OTP");
        message.setText("Hi " + firstName + ",\n\n"
                + "Your one time password to reset your Fundoo Notes password is:\n\n"
                + "    " + otp + "\n\n"
                + "It is valid for " + validMinutes + " minutes. If you did not ask for a password reset, you can ignore this email.\n\n"
                + "Fundoo Notes");
        try {
            mailSender.send(message);
            // the OTP itself is never written to the log
            log.info("OTP email sent to {}", to);
        } catch (MailException e) {
            log.error("Could not send OTP email to {}: {}", to, e.getMessage());
            throw new EmailDeliveryException("Could not send the OTP email, please try again later", e);
        }
    }
}
