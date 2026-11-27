package com.fundoo.notes.util;

import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.Note;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NoteMapperTest {

    @Test
    void testNoteToResponse_WithSortedLabelsAndReminder() {
        Note note = new Note("Report", "Q3", null);
        note.setId(1L);
        note.setPinned(true);
        note.setReminderTime(LocalDateTime.of(2030, 1, 1, 9, 0));
        note.setReminderSent(true);
        Label work = new Label("work", null);
        work.setId(2L);
        Label admin = new Label("Admin", null);
        admin.setId(3L);
        note.addLabel(work);
        note.addLabel(admin);

        NoteResponseDto response = NoteMapper.toResponse(note);

        assertEquals("Report", response.getTitle());
        assertTrue(response.isPinned());
        assertEquals(LocalDateTime.of(2030, 1, 1, 9, 0), response.getReminderTime());
        assertTrue(response.isReminderSent());
        assertEquals("Admin", response.getLabels().get(0).getName());
        assertEquals("work", response.getLabels().get(1).getName());
    }

    @Test
    void testLabelToResponse() {
        Label label = new Label("Home", null);
        label.setId(5L);
        assertEquals(5L, NoteMapper.toResponse(label).getId());
    }
}
