package com.fundoo.notes.controller;

import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.service.LabelService;
import com.fundoo.notes.service.NoteService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    private static final String TOKEN = "Bearer token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private LabelService labelService;

    private NoteResponseDto note(boolean pinned, boolean archived, boolean trashed) {
        return new NoteResponseDto(1L, "Shopping", "Milk", pinned, archived, trashed, null, null);
    }

    @Test
    void testCreateNote_Returns201() throws Exception {
        when(noteService.createNote(any(), eq(TOKEN))).thenReturn(note(false, false, false));
        mockMvc.perform(post("/api/notes").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Shopping\", \"description\": \"Milk\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Shopping"));
    }

    @Test
    void testCreateNote_BlankTitle_Returns400() throws Exception {
        mockMvc.perform(post("/api/notes").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").value("Title is required"));
    }

    @Test
    void testMissingToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/notes")).andExpect(status().isUnauthorized());
        verifyNoInteractions(noteService);
    }

    @Test
    void testInvalidToken_Returns401() throws Exception {
        when(noteService.getAllNotes("bad")).thenThrow(new InvalidCredentialsException("Invalid or expired token"));
        mockMvc.perform(get("/api/notes").header("Authorization", "bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void testGetAllNotes() throws Exception {
        when(noteService.getAllNotes(TOKEN)).thenReturn(List.of(note(true, false, false)));
        mockMvc.perform(get("/api/notes").header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pinned").value(true));
    }

    @Test
    void testPinArchiveTrash() throws Exception {
        when(noteService.pinNote(1L, TOKEN)).thenReturn(note(true, false, false));
        when(noteService.archiveNote(1L, TOKEN)).thenReturn(note(false, true, false));
        when(noteService.trashNote(1L, TOKEN)).thenReturn(note(false, false, true));
        mockMvc.perform(patch("/api/notes/1/pin").header("Authorization", TOKEN)).andExpect(jsonPath("$.pinned").value(true));
        mockMvc.perform(patch("/api/notes/1/archive").header("Authorization", TOKEN)).andExpect(jsonPath("$.archived").value(true));
        mockMvc.perform(patch("/api/notes/1/trash").header("Authorization", TOKEN)).andExpect(jsonPath("$.trashed").value(true));
    }

    @Test
    void testNoteNotFound_Returns404() throws Exception {
        when(noteService.pinNote(99L, TOKEN)).thenThrow(new NoteNotFoundException(99L));
        mockMvc.perform(patch("/api/notes/99/pin").header("Authorization", TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Note not found with id: 99"));
    }

    @Test
    void testUpdateNote() throws Exception {
        when(noteService.updateNote(eq(1L), any(), eq(TOKEN))).thenReturn(new NoteResponseDto(1L, "Edited", "Body", false, false, false, null, null));
        mockMvc.perform(put("/api/notes/1").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Edited\", \"description\": \"Body\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Edited"));
    }

    @Test
    void testDeleteNote_Returns204() throws Exception {
        mockMvc.perform(delete("/api/notes/1").header("Authorization", TOKEN)).andExpect(status().isNoContent());
        verify(noteService).deleteNote(1L, TOKEN);
    }

    @Test
    void testDeleteNote_NotInTrash_Returns400() throws Exception {
        doThrow(new IllegalArgumentException("Move the note to trash before deleting it forever")).when(noteService).deleteNote(1L, TOKEN);
        mockMvc.perform(delete("/api/notes/1").header("Authorization", TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Move the note to trash before deleting it forever"));
    }

    @Test
    void testArchivedTrashAndSearchLists() throws Exception {
        when(noteService.getArchivedNotes(TOKEN)).thenReturn(List.of(note(false, true, false)));
        when(noteService.getTrashedNotes(TOKEN)).thenReturn(List.of(note(false, false, true)));
        when(noteService.searchNotes("shop", TOKEN)).thenReturn(List.of(note(false, false, false)));
        mockMvc.perform(get("/api/notes/archived").header("Authorization", TOKEN)).andExpect(jsonPath("$[0].archived").value(true));
        mockMvc.perform(get("/api/notes/trash").header("Authorization", TOKEN)).andExpect(jsonPath("$[0].trashed").value(true));
        mockMvc.perform(get("/api/notes/search").param("keyword", "shop").header("Authorization", TOKEN)).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testAddAndRemoveLabel() throws Exception {
        NoteResponseDto labelled = note(false, false, false);
        labelled.setLabels(java.util.List.of(new com.fundoo.notes.dto.response.LabelResponseDto(4L, "Work")));
        when(labelService.addLabelToNote(1L, 4L, TOKEN)).thenReturn(labelled);
        when(labelService.removeLabelFromNote(1L, 4L, TOKEN)).thenReturn(note(false, false, false));
        mockMvc.perform(post("/api/notes/1/labels/4").header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels[0].name").value("Work"));
        mockMvc.perform(delete("/api/notes/1/labels/4").header("Authorization", TOKEN))
                .andExpect(jsonPath("$.labels.length()").value(0));
    }
}
