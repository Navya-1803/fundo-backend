package com.fundoo.notes.dto.response;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class LabelResponseDtoTest {

    @Test
    void testConstructorAndSetters() {
        LabelResponseDto dto = new LabelResponseDto(1L, "Work");
        assertEquals(1L, dto.getId());
        assertEquals("Work", dto.getName());
        dto.setId(2L);
        dto.setName("Home");
        assertEquals(2L, dto.getId());
        assertEquals("Home", dto.getName());
        assertInstanceOf(Serializable.class, dto);
    }
}
