package com.fundoo.notes.service;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;

import java.util.List;

/**
 * Sets, lists and removes reminders on the notes of the logged in user.
 *
 * @author Navya Raj
 */
public interface ReminderService {

    NoteResponseDto setReminder(ReminderRequestDto requestDto, String token);

    List<NoteResponseDto> getReminders(String token);

    NoteResponseDto removeReminder(Long noteId, String token);
}
