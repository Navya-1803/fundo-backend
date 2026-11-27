package com.fundoo.notes.exception;

/**
 * Thrown when an email (for example the password reset OTP) cannot be sent.
 *
 * @author Navya Raj
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
