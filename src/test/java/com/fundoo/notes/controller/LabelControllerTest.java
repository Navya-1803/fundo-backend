package com.fundoo.notes.controller;

import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.exception.LabelNotFoundException;
import com.fundoo.notes.service.LabelService;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabelController.class)
class LabelControllerTest {

    private static final String TOKEN = "Bearer token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabelService labelService;

    @Test
    void testCreateLabel_Returns201() throws Exception {
        when(labelService.createLabel(any(), eq(TOKEN))).thenReturn(new LabelResponseDto(1L, "Work"));
        mockMvc.perform(post("/api/labels").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Work\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Work"));
    }

    @Test
    void testCreateLabel_BlankName_Returns400() throws Exception {
        mockMvc.perform(post("/api/labels").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").value("Label name is required"));
    }

    @Test
    void testListRenameAndDelete() throws Exception {
        when(labelService.getLabels(TOKEN)).thenReturn(List.of(new LabelResponseDto(1L, "Work")));
        when(labelService.renameLabel(eq(1L), any(), eq(TOKEN))).thenReturn(new LabelResponseDto(1L, "Office"));
        mockMvc.perform(get("/api/labels").header("Authorization", TOKEN)).andExpect(jsonPath("$[0].name").value("Work"));
        mockMvc.perform(put("/api/labels/1").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Office\"}"))
                .andExpect(jsonPath("$.name").value("Office"));
        mockMvc.perform(delete("/api/labels/1").header("Authorization", TOKEN)).andExpect(status().isNoContent());
        verify(labelService).deleteLabel(1L, TOKEN);
    }

    @Test
    void testNotesByLabel() throws Exception {
        when(labelService.getNotesByLabel(1L, TOKEN)).thenReturn(List.of(new NoteResponseDto(3L, "Report", "", false, false, false, null, null)));
        mockMvc.perform(get("/api/labels/1/notes").header("Authorization", TOKEN)).andExpect(jsonPath("$[0].title").value("Report"));
    }

    @Test
    void testUnknownLabel_Returns404() throws Exception {
        when(labelService.getNotesByLabel(9L, TOKEN)).thenThrow(new LabelNotFoundException(9L));
        mockMvc.perform(get("/api/labels/9/notes").header("Authorization", TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Label not found with id: 9"));
    }
}
