package com.fundoo.notes.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LabelRequestDtoTest {

    @Test
    void testValidation() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(new LabelRequestDto("Work")).isEmpty());
            assertEquals("Label name is required", validator.validate(new LabelRequestDto(" ")).iterator().next().getMessage());
            assertFalse(validator.validate(new LabelRequestDto("a".repeat(51))).isEmpty());
        }
    }

    @Test
    void testGetterAndSetter() {
        LabelRequestDto dto = new LabelRequestDto();
        dto.setName("Home");
        assertEquals("Home", dto.getName());
    }
}
