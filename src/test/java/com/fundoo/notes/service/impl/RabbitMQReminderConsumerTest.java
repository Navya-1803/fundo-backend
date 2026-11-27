package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.service.EmailService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class RabbitMQReminderConsumerTest {

    private final EmailService emailService = mock(EmailService.class);
    private final RabbitMQReminderConsumer consumer = new RabbitMQReminderConsumer(emailService);
    private final ReminderEvent event = new ReminderEvent(1L, 2L, "Pay bills", LocalDateTime.of(2030, 1, 1, 9, 0), "Due today");

    @Test
    void testConsumeReminder_SendsEmail(CapturedOutput output) {
        consumer.consumeReminder(event);
        verify(emailService).sendReminderEmail(event);
        assertTrue(output.getOut().contains("Reminder for note id: 1 emailed to user id: 2"));
    }

    @Test
    void testConsumeReminder_MailServerDown_IsLogged(CapturedOutput output) {
        doThrow(new EmailDeliveryException("Could not send the Reminder email", null)).when(emailService).sendReminderEmail(any());
        assertDoesNotThrow(() -> consumer.consumeReminder(event));
        assertTrue(output.getOut().contains("could not be emailed"));
    }
}
