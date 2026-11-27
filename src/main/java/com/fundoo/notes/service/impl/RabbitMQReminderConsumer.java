package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumes due reminder events from RabbitMQ and emails them to the owner
 * of the note.
 *
 * @author Navya Raj
 */
// listeners can be switched off, for example in tests where no broker is running
@Component
@ConditionalOnProperty(name = "app.messaging.listeners-enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQReminderConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQReminderConsumer.class);

    private final EmailService emailService;

    public RabbitMQReminderConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${app.rabbitmq.reminder-queue}")
    public void consumeReminder(ReminderEvent event) {
        log.info("Received reminder event: {}", event);
        sendNotification(event);
    }

    void sendNotification(ReminderEvent event) {
        try {
            emailService.sendReminderEmail(event);
            log.info("Reminder for note id: {} emailed to user id: {}", event.getNoteId(), event.getUserId());
        } catch (EmailDeliveryException e) {
            // the message is not retried endlessly, the failure is logged instead
            log.error("Reminder for note id: {} could not be emailed: {}", event.getNoteId(), e.getMessage());
        }
    }
}
