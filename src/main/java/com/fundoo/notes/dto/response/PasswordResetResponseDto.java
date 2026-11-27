package com.fundoo.notes.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response of the forgot / reset password endpoints. The OTP is included
 * only because no mail server is connected; in production it would be
 * emailed to the user instead.
 *
 * @author Navya Raj
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordResetResponseDto {

    private String message;
    private String otp;

    public PasswordResetResponseDto() {
    }

    public PasswordResetResponseDto(String message, String otp) {
        this.message = message;
        this.otp = otp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
