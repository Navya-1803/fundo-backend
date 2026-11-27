package com.fundoo.notes.controller;

import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.service.ReminderService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReminderController.class)
class ReminderControllerTest {

    private static final String TOKEN = "Bearer token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReminderService reminderService;

    private NoteResponseDto noteWithReminder() {
        NoteResponseDto note = new NoteResponseDto(1L, "Call", "", false, false, false, null, null);
        note.setReminderTime(LocalDateTime.of(2030, 1, 1, 9, 0));
        return note;
    }

    private String body(LocalDateTime time) {
        return "{\"noteId\": 1, \"reminderTime\": \"" + time.withNano(0) + "\", \"message\": \"Call mom\"}";
    }

    @Test
    void testCreateReminder_ReturnsNote() throws Exception {
        when(reminderService.setReminder(any(), eq(TOKEN))).thenReturn(noteWithReminder());
        mockMvc.perform(post("/api/reminders").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content(body(LocalDateTime.now().plusDays(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reminderTime").value("2030-01-01T09:00:00"));
    }

    @Test
    void testPastReminderTime_Returns400() throws Exception {
        mockMvc.perform(post("/api/reminders").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content(body(LocalDateTime.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.reminderTime").value("Reminder time must be in the future"));
        verifyNoInteractions(reminderService);
    }

    @Test
    void testNoteOfOtherUser_Returns404() throws Exception {
        when(reminderService.setReminder(any(), eq(TOKEN))).thenThrow(new NoteNotFoundException(1L));
        mockMvc.perform(post("/api/reminders").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content(body(LocalDateTime.now().plusDays(1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListAndRemoveReminders() throws Exception {
        when(reminderService.getReminders(TOKEN)).thenReturn(List.of(noteWithReminder()));
        when(reminderService.removeReminder(1L, TOKEN)).thenReturn(new NoteResponseDto(1L, "Call", "", false, false, false, null, null));
        mockMvc.perform(get("/api/reminders").header("Authorization", TOKEN)).andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(delete("/api/reminders/1").header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reminderTime").doesNotExist());
    }
}
