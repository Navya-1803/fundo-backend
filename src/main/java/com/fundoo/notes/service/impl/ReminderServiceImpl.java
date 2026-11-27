package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
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
 * Stores the reminder time and message on the note. The email itself is
 * sent later by the ReminderScheduler when the reminder time is reached.
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

    public ReminderServiceImpl(NoteRepository noteRepository, TokenService tokenService) {
        this.noteRepository = noteRepository;
        this.tokenService = tokenService;
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
        note.setReminderMessage(requestDto.getMessage());
        // a new time means a new email, even if an earlier reminder was already sent
        note.setReminderSent(false);
        Note saved = noteRepository.save(note);
        log.info("Reminder set on note id: {} for {}", saved.getId(), saved.getReminderTime());
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
        note.setReminderMessage(null);
        note.setReminderSent(false);
        log.info("Reminder removed from note id: {}", noteId);
        return NoteMapper.toResponse(noteRepository.save(note));
    }

    private Note findOwnedNote(Long noteId, Long userId) {
        return noteRepository.findByIdAndUserId(noteId, userId).orElseThrow(() -> new NoteNotFoundException(noteId));
    }
}
