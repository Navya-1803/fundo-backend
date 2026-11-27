package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.service.ReminderEventPublisher;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReminderServiceImplTest {

    private static final String TOKEN = "Bearer token";
    private static final LocalDateTime TIME = LocalDateTime.of(2030, 1, 1, 9, 0);

    private NoteRepository noteRepository;
    private ReminderEventPublisher publisher;
    private ReminderServiceImpl reminderService;
    private Note note;

    @BeforeEach
    void setUp() {
        noteRepository = mock(NoteRepository.class);
        TokenService tokenService = mock(TokenService.class);
        publisher = mock(ReminderEventPublisher.class);
        reminderService = new ReminderServiceImpl(noteRepository, tokenService, publisher);
        when(tokenService.extractUserId(TOKEN)).thenReturn(1L);
        note = new Note("Call mom", "", new User());
        note.setId(5L);
        when(noteRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testSetReminder_SavesAndPublishes() {
        NoteResponseDto response = reminderService.setReminder(new ReminderRequestDto(5L, TIME, "Ring"), TOKEN);
        assertEquals(TIME, response.getReminderTime());
        ArgumentCaptor<ReminderEvent> captor = ArgumentCaptor.forClass(ReminderEvent.class);
        verify(publisher).publish(captor.capture());
        assertEquals("Ring", captor.getValue().getMessage());
        assertEquals(1L, captor.getValue().getUserId());
    }

    @Test
    void testSetReminder_BrokerDown_StillSaved() {
        doThrow(new IllegalStateException("Reminder service is currently unavailable")).when(publisher).publish(any());
        NoteResponseDto response = reminderService.setReminder(new ReminderRequestDto(5L, TIME, null), TOKEN);
        assertEquals(TIME, response.getReminderTime());
        verify(noteRepository).save(note);
    }

    @Test
    void testSetReminder_TrashedNoteRejected() {
        note.setTrashed(true);
        assertThrows(IllegalArgumentException.class, () -> reminderService.setReminder(new ReminderRequestDto(5L, TIME, null), TOKEN));
        verifyNoInteractions(publisher);
    }

    @Test
    void testSetReminder_UnknownNote() {
        assertThrows(NoteNotFoundException.class, () -> reminderService.setReminder(new ReminderRequestDto(9L, TIME, null), TOKEN));
    }

    @Test
    void testGetAndRemoveReminders() {
        note.setReminderTime(TIME);
        when(noteRepository.findByUserIdAndReminderTimeIsNotNullAndTrashedFalseOrderByReminderTimeAsc(1L)).thenReturn(List.of(note));
        assertEquals(1, reminderService.getReminders(TOKEN).size());
        assertNull(reminderService.removeReminder(5L, TOKEN).getReminderTime());
        assertNull(note.getReminderTime());
    }
}
