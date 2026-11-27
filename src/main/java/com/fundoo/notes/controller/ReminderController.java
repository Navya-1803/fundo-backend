package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.service.ReminderService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Reminders on notes. The reminder is stored with the note and a
 * notification event is sent to RabbitMQ in the background.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private static final Logger log = LoggerFactory.getLogger(ReminderController.class);

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<NoteResponseDto> createReminder(@Valid @RequestBody ReminderRequestDto requestDto,
                                                          @RequestHeader("Authorization") String token) {
        log.debug("POST /api/reminders for note id: {}", requestDto.getNoteId());
        return ResponseEntity.ok(reminderService.setReminder(requestDto, token));
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getReminders(@RequestHeader("Authorization") String token) {
        log.debug("GET /api/reminders");
        return ResponseEntity.ok(reminderService.getReminders(token));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<NoteResponseDto> removeReminder(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        log.debug("DELETE /api/reminders/{}", noteId);
        return ResponseEntity.ok(reminderService.removeReminder(noteId, token));
    }
}
