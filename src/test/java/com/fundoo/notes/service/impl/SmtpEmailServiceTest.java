package com.fundoo.notes.service.impl;

import com.fundoo.notes.exception.EmailDeliveryException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class SmtpEmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final SmtpEmailService emailService = new SmtpEmailService(mailSender, "no-reply@fundoonotes.com");

    @Test
    void testSendOtpEmail_BuildsMessage(CapturedOutput output) {
        emailService.sendOtpEmail("navya@example.com", "Navya", "482913", 5);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("no-reply@fundoonotes.com", message.getFrom());
        assertArrayEquals(new String[]{"navya@example.com"}, message.getTo());
        assertTrue(message.getSubject().contains("OTP"));
        assertTrue(message.getText().contains("482913"));
        assertTrue(message.getText().contains("Hi Navya"));
        assertTrue(message.getText().contains("valid for 5 minutes"));
        // the OTP must not appear in the logs
        assertFalse(output.getOut().contains("482913"));
    }

    @Test
    void testSendOtpEmail_MailServerDown() {
        doThrow(new MailSendException("Connection refused")).when(mailSender).send(any(SimpleMailMessage.class));
        assertThrows(EmailDeliveryException.class, () -> emailService.sendOtpEmail("navya@example.com", "Navya", "482913", 5));
    }
}
