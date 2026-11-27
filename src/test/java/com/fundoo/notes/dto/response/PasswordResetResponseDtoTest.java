package com.fundoo.notes.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetResponseDtoTest {

    @Test
    void testConstructorAndSetters() {
        PasswordResetResponseDto dto = new PasswordResetResponseDto("OTP sent", "123456");
        assertEquals("OTP sent", dto.getMessage());
        assertEquals("123456", dto.getOtp());
        dto.setMessage("done");
        dto.setOtp(null);
        assertEquals("done", dto.getMessage());
        assertNull(dto.getOtp());
    }
}
