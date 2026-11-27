package com.fundoo.notes.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;

import static org.junit.jupiter.api.Assertions.*;

class EmailDeliveryExceptionTest {

    @Test
    void testMessageAndCause() {
        MailSendException cause = new MailSendException("connection refused");
        EmailDeliveryException exception = new EmailDeliveryException("Could not send the OTP email", cause);
        assertEquals("Could not send the OTP email", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
