package com.fundoo.notes.scheduler;

import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.service.EmailService;
import com.fundoo.notes.service.ReminderEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReminderSchedulerTest {

    private NoteRepository noteRepository;
    private ReminderEventPublisher publisher;
    private EmailService emailService;
    private ReminderScheduler scheduler;
    private Note note;

    @BeforeEach
    void setUp() {
        noteRepository = mock(NoteRepository.class);
        publisher = mock(ReminderEventPublisher.class);
        emailService = mock(EmailService.class);
        CacheManager cacheManager = new ConcurrentMapCacheManager("notes");
        scheduler = new ReminderScheduler(noteRepository, publisher, emailService, cacheManager);

        User owner = new User("Navya", "Raj", "navya@example.com", "hash");
        owner.setId(1L);
        note = new Note("Call mom", "Sunday evening", owner);
        note.setId(3L);
        note.setReminderTime(LocalDateTime.now().minusMinutes(1));
        note.setReminderMessage("Weekly call");
        when(noteRepository.findByReminderTimeLessThanEqualAndReminderSentFalseAndTrashedFalse(any())).thenReturn(List.of(note));
    }

    @Test
    void testDueReminder_IsQueuedWithOwnerDetailsAndMarkedSent() {
        scheduler.sendDueReminders();
        ArgumentCaptor<ReminderEvent> captor = ArgumentCaptor.forClass(ReminderEvent.class);
        verify(publisher).publish(captor.capture());
        ReminderEvent event = captor.getValue();
        assertEquals("navya@example.com", event.getUserEmail());
        assertEquals("Navya", event.getUserFirstName());
        assertEquals("Weekly call", event.getMessage());
        assertEquals("Sunday evening", event.getNoteDescription());
        assertTrue(note.isReminderSent());
        verify(noteRepository).save(note);
        verifyNoInteractions(emailService);
    }

    @Test
    void testRabbitMqDown_EmailIsSentDirectly() {
        doThrow(new IllegalStateException("Reminder service is currently unavailable")).when(publisher).publish(any());
        scheduler.sendDueReminders();
        verify(emailService).sendReminderEmail(any(ReminderEvent.class));
        assertTrue(note.isReminderSent());
    }

    @Test
    void testEverythingDown_ReminderStaysUnsentForNextRun() {
        doThrow(new IllegalStateException("down")).when(publisher).publish(any());
        doThrow(new EmailDeliveryException("mail down", null)).when(emailService).sendReminderEmail(any());
        scheduler.sendDueReminders();
        assertFalse(note.isReminderSent());
        verify(noteRepository, never()).save(any());
    }

    @Test
    void testNothingDue() {
        when(noteRepository.findByReminderTimeLessThanEqualAndReminderSentFalseAndTrashedFalse(any())).thenReturn(List.of());
        scheduler.sendDueReminders();
        verifyNoInteractions(publisher, emailService);
    }
}
