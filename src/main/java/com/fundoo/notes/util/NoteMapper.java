package com.fundoo.notes.util;

import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.Note;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Converts entities into response DTOs. Kept in one place so every service
 * returns notes and labels in exactly the same shape.
 *
 * @author Navya Raj
 */
public final class NoteMapper {

    private NoteMapper() {
        // static helper, no objects needed
    }

    public static NoteResponseDto toResponse(Note note) {
        NoteResponseDto response = new NoteResponseDto(note.getId(), note.getTitle(), note.getDescription(), note.isPinned(),
                note.isArchived(), note.isTrashed(), note.getCreatedAt(), note.getUpdatedAt());
        response.setReminderTime(note.getReminderTime());
        // labels sorted by name, in a mutable list so the cache can serialize it
        List<LabelResponseDto> labels = new ArrayList<>(note.getLabels().stream()
                .sorted(Comparator.comparing(Label::getName, String.CASE_INSENSITIVE_ORDER))
                .map(NoteMapper::toResponse)
                .toList());
        response.setLabels(labels);
        return response;
    }

    public static LabelResponseDto toResponse(Label label) {
        return new LabelResponseDto(label.getId(), label.getName());
    }
}
