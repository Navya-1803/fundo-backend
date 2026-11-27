package com.fundoo.notes.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetResponseDtoTest {

    @Test
    void testConstructorAndSetter() {
        PasswordResetResponseDto dto = new PasswordResetResponseDto("OTP sent");
        assertEquals("OTP sent", dto.getMessage());
        dto.setMessage("done");
        assertEquals("done", dto.getMessage());
    }

    @Test
    void testHasNoOtpField() {
        // the OTP must only travel by email
        assertThrows(NoSuchFieldException.class, () -> PasswordResetResponseDto.class.getDeclaredField("otp"));
    }
}
