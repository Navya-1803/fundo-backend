package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.service.ReminderEventPublisher;
import com.fundoo.notes.service.ReminderService;
import com.fundoo.notes.service.TokenService;
import com.fundoo.notes.util.NoteMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Stores the reminder time on the note (so it is never lost) and then
 * publishes a reminder event to RabbitMQ for the notification. If the
 * broker is down the reminder is still saved and the problem is logged.
 *
 * @author Navya Raj
 */
@Service
@Transactional
public class ReminderServiceImpl implements ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderServiceImpl.class);

    private static final String USER_KEY = "@jwtTokenService.extractUserId(#token)";

    private final NoteRepository noteRepository;
    private final TokenService tokenService;
    private final ReminderEventPublisher reminderEventPublisher;

    public ReminderServiceImpl(NoteRepository noteRepository, TokenService tokenService,
                               ReminderEventPublisher reminderEventPublisher) {
        this.noteRepository = noteRepository;
        this.tokenService = tokenService;
        this.reminderEventPublisher = reminderEventPublisher;
    }

    @Override
    @CacheEvict(value = NoteServiceImpl.NOTES_CACHE, key = USER_KEY)
    public NoteResponseDto setReminder(ReminderRequestDto requestDto, String token) {
        Long userId = tokenService.extractUserId(token);
        Note note = findOwnedNote(requestDto.getNoteId(), userId);
        if (note.isTrashed()) {
            throw new IllegalArgumentException("A reminder cannot be set on a note in trash");
        }
        note.setReminderTime(requestDto.getReminderTime());
        Note saved = noteRepository.save(note);
        log.info("Reminder set on note id: {} for {}", saved.getId(), saved.getReminderTime());
        publishEvent(new ReminderEvent(saved.getId(), userId, saved.getTitle(), saved.getReminderTime(), requestDto.getMessage()));
        return NoteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getReminders(String token) {
        Long userId = tokenService.extractUserId(token);
        log.info("Fetching reminders for user id: {}", userId);
        return noteRepository.findByUserIdAndReminderTimeIsNotNullAndTrashedFalseOrderByReminderTimeAsc(userId).stream()
                .map(NoteMapper::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(value = NoteServiceImpl.NOTES_CACHE, key = USER_KEY)
    public NoteResponseDto removeReminder(Long noteId, String token) {
        Note note = findOwnedNote(noteId, tokenService.extractUserId(token));
        note.setReminderTime(null);
        log.info("Reminder removed from note id: {}", noteId);
        return NoteMapper.toResponse(noteRepository.save(note));
    }

    // the notification is best effort, the stored reminder is what matters
    private void publishEvent(ReminderEvent event) {
        try {
            reminderEventPublisher.publish(event);
        } catch (IllegalStateException e) {
            log.warn("Reminder saved but notification could not be queued: {}", e.getMessage());
        }
    }

    private Note findOwnedNote(Long noteId, Long userId) {
        return noteRepository.findByIdAndUserId(noteId, userId).orElseThrow(() -> new NoteNotFoundException(noteId));
    }

}
