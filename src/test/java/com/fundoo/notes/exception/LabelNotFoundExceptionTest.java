package com.fundoo.notes.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LabelNotFoundExceptionTest {

    @Test
    void testMessageContainsId() {
        assertEquals("Label not found with id: 8", new LabelNotFoundException(8L).getMessage());
    }
}
