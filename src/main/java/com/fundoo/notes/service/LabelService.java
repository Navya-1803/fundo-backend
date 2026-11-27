package com.fundoo.notes.service;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.dto.response.NoteResponseDto;

import java.util.List;

/**
 * Label management and labelling of notes for the logged in user.
 *
 * @author Navya Raj
 */
public interface LabelService {

    LabelResponseDto createLabel(LabelRequestDto requestDto, String token);

    List<LabelResponseDto> getLabels(String token);

    LabelResponseDto renameLabel(Long labelId, LabelRequestDto requestDto, String token);

    void deleteLabel(Long labelId, String token);

    List<NoteResponseDto> getNotesByLabel(Long labelId, String token);

    NoteResponseDto addLabelToNote(Long noteId, Long labelId, String token);

    NoteResponseDto removeLabelFromNote(Long noteId, Long labelId, String token);
}
