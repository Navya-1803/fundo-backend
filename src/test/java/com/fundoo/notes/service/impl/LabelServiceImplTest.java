package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.LabelNotFoundException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.LabelRepository;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LabelServiceImplTest {

    private static final String TOKEN = "Bearer token";

    private LabelRepository labelRepository;
    private NoteRepository noteRepository;
    private LabelServiceImpl labelService;
    private User user;
    private Label work;

    @BeforeEach
    void setUp() {
        labelRepository = mock(LabelRepository.class);
        noteRepository = mock(NoteRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TokenService tokenService = mock(TokenService.class);
        labelService = new LabelServiceImpl(labelRepository, noteRepository, userRepository, tokenService);
        user = new User("Navya", "Raj", "navya@example.com", "hash");
        user.setId(1L);
        work = new Label("Work", user);
        work.setId(4L);
        when(tokenService.extractUserId(TOKEN)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(labelRepository.findByIdAndUserId(4L, 1L)).thenReturn(Optional.of(work));
        when(labelRepository.save(any(Label.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testCreateLabel() {
        assertEquals("Travel", labelService.createLabel(new LabelRequestDto(" Travel "), TOKEN).getName());
    }

    @Test
    void testCreateLabel_DuplicateRejected() {
        when(labelRepository.existsByUserIdAndNameIgnoreCase(1L, "work")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> labelService.createLabel(new LabelRequestDto("work"), TOKEN));
    }

    @Test
    void testGetLabels() {
        when(labelRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of(work));
        assertEquals("Work", labelService.getLabels(TOKEN).get(0).getName());
    }

    @Test
    void testRenameLabel() {
        assertEquals("Office", labelService.renameLabel(4L, new LabelRequestDto("Office"), TOKEN).getName());
        // only the case changes, no duplicate check is needed
        assertEquals("office", labelService.renameLabel(4L, new LabelRequestDto("office"), TOKEN).getName());
    }

    @Test
    void testDeleteLabel_RemovesItFromNotes() {
        Note note = new Note("Report", "", user);
        note.addLabel(work);
        when(noteRepository.findByLabelsId(4L)).thenReturn(List.of(note));
        labelService.deleteLabel(4L, TOKEN);
        assertTrue(note.getLabels().isEmpty());
        verify(labelRepository).delete(work);
    }

    @Test
    void testUnknownLabel() {
        assertThrows(LabelNotFoundException.class, () -> labelService.deleteLabel(99L, TOKEN));
        assertThrows(LabelNotFoundException.class, () -> labelService.getNotesByLabel(99L, TOKEN));
    }

    @Test
    void testAddAndRemoveLabelOnNote() {
        Note note = new Note("Report", "", user);
        note.setId(7L);
        when(noteRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(note));
        assertEquals(1, labelService.addLabelToNote(7L, 4L, TOKEN).getLabels().size());
        assertEquals(0, labelService.removeLabelFromNote(7L, 4L, TOKEN).getLabels().size());
        assertThrows(NoteNotFoundException.class, () -> labelService.addLabelToNote(8L, 4L, TOKEN));
    }

    @Test
    void testGetNotesByLabel() {
        Note note = new Note("Report", "", user);
        when(noteRepository.findByUserIdAndLabelsIdAndTrashedFalse(1L, 4L)).thenReturn(List.of(note));
        assertEquals(1, labelService.getNotesByLabel(4L, TOKEN).size());
    }
}
