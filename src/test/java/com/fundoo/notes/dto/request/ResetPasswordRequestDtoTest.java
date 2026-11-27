package com.fundoo.notes.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordRequestDtoTest {

    @Test
    void testValidation() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(new ResetPasswordRequestDto("navya@example.com", "123456", "newpass123")).isEmpty());
            assertEquals(3, validator.validate(new ResetPasswordRequestDto("bad", "12", "weak")).size());
        }
    }

    @Test
    void testGettersAndSetters() {
        ResetPasswordRequestDto dto = new ResetPasswordRequestDto();
        dto.setEmail("a@example.com");
        dto.setOtp("111111");
        dto.setNewPassword("pass1234");
        assertEquals("a@example.com", dto.getEmail());
        assertEquals("111111", dto.getOtp());
        assertEquals("pass1234", dto.getNewPassword());
    }
}
