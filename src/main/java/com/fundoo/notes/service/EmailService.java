package com.fundoo.notes.service;

/**
 * Sends emails to users.
 *
 * @author Navya Raj
 */
public interface EmailService {

    /**
     * Sends the one time password used to reset the password.
     *
     * @throws com.fundoo.notes.exception.EmailDeliveryException if the mail server rejects or cannot be reached
     */
    void sendOtpEmail(String to, String firstName, String otp, long validMinutes);
}
