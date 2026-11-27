package com.fundoo.notes.dto.response;

/**
 * Response of the forgot / reset password endpoints. The OTP itself is
 * never returned, it is sent to the user's email address.
 *
 * @author Navya Raj
 */
public class PasswordResetResponseDto {

    private String message;

    public PasswordResetResponseDto() {
    }

    public PasswordResetResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
