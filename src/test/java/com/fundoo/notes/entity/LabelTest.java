package com.fundoo.notes.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LabelTest {

    @Test
    void testConstructorAndSetters() {
        User owner = new User();
        Label label = new Label("Work", owner);
        assertEquals("Work", label.getName());
        assertSame(owner, label.getUser());
        label.setId(2L);
        label.setName("Home");
        label.setUser(null);
        assertEquals(2L, label.getId());
        assertEquals("Home", label.getName());
        assertNull(label.getUser());
        assertTrue(label.toString().contains("Home"));
    }

    @Test
    void testCreatedAt() {
        Label label = new Label();
        label.onCreate();
        assertNotNull(label.getCreatedAt());
    }

    @Test
    void testNoteLabels() {
        Note note = new Note();
        Label label = new Label("Work", null);
        note.addLabel(label);
        assertTrue(note.getLabels().contains(label));
        note.removeLabel(label);
        assertTrue(note.getLabels().isEmpty());
    }
}
