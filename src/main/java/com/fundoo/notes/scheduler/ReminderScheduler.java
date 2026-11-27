package com.fundoo.notes.scheduler;

import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.service.EmailService;
import com.fundoo.notes.service.ReminderEventPublisher;
import com.fundoo.notes.service.impl.NoteServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Checks every minute for reminders whose time has come. Each due reminder
 * is queued on RabbitMQ (the consumer emails it); when RabbitMQ is down the
 * email is sent directly. A reminder is marked as sent so it goes out once.
 *
 * @author Navya Raj
 */
@Component
@ConditionalOnProperty(name = "app.reminder.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final NoteRepository noteRepository;
    private final ReminderEventPublisher reminderEventPublisher;
    private final EmailService emailService;
    private final CacheManager cacheManager;

    public ReminderScheduler(NoteRepository noteRepository, ReminderEventPublisher reminderEventPublisher,
                             EmailService emailService, CacheManager cacheManager) {
        this.noteRepository = noteRepository;
        this.reminderEventPublisher = reminderEventPublisher;
        this.emailService = emailService;
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedDelayString = "${app.reminder.check-interval-ms:60000}", initialDelay = 10000)
    @Transactional
    public void sendDueReminders() {
        List<Note> dueNotes = noteRepository.findByReminderTimeLessThanEqualAndReminderSentFalseAndTrashedFalse(LocalDateTime.now());
        if (dueNotes.isEmpty()) {
            return;
        }
        log.info("Found {} due reminder(s)", dueNotes.size());
        for (Note note : dueNotes) {
            if (deliver(toEvent(note))) {
                note.setReminderSent(true);
                noteRepository.save(note);
                evictNotesCache(note.getUser().getId());
            }
        }
    }

    // returns true when the reminder was queued or emailed, false means try again next run
    private boolean deliver(ReminderEvent event) {
        try {
            reminderEventPublisher.publish(event);
            return true;
        } catch (IllegalStateException e) {
            log.warn("RabbitMQ unavailable, emailing reminder for note id: {} directly", event.getNoteId());
        }
        try {
            emailService.sendReminderEmail(event);
            return true;
        } catch (EmailDeliveryException e) {
            log.error("Reminder for note id: {} not sent, will retry: {}", event.getNoteId(), e.getMessage());
            return false;
        }
    }

    private ReminderEvent toEvent(Note note) {
        User owner = note.getUser();
        ReminderEvent event = new ReminderEvent(note.getId(), owner.getId(), note.getTitle(), note.getReminderTime(), note.getReminderMessage());
        event.setUserEmail(owner.getEmail());
        event.setUserFirstName(owner.getFirstName());
        event.setNoteDescription(note.getDescription());
        return event;
    }

    // the dashboard shows the sent state, so the owner's cached list is dropped
    private void evictNotesCache(Long userId) {
        try {
            Cache cache = cacheManager.getCache(NoteServiceImpl.NOTES_CACHE);
            if (cache != null) {
                cache.evict(userId);
            }
        } catch (RuntimeException e) {
            log.warn("Could not evict notes cache for user id: {}: {}", userId, e.getMessage());
        }
    }
}
