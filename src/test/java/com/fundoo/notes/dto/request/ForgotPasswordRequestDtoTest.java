package com.fundoo.notes.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForgotPasswordRequestDtoTest {

    @Test
    void testValidation() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(new ForgotPasswordRequestDto("navya@example.com")).isEmpty());
            assertFalse(validator.validate(new ForgotPasswordRequestDto("bad")).isEmpty());
        }
    }

    @Test
    void testGetterAndSetter() {
        ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto();
        dto.setEmail("a@example.com");
        assertEquals("a@example.com", dto.getEmail());
    }
}
