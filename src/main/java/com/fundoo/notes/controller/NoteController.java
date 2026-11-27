package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.service.LabelService;
import com.fundoo.notes.service.NoteService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for notes. The JWT from login is sent in the
 * Authorization header of every request.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);

    private final NoteService noteService;
    private final LabelService labelService;

    public NoteController(NoteService noteService, LabelService labelService) {
        this.noteService = noteService;
        this.labelService = labelService;
    }

    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(@Valid @RequestBody NoteRequestDto requestDto,
                                                      @RequestHeader("Authorization") String token) {
        log.debug("POST /api/notes");
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(requestDto, token));
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getAllNotes(@RequestHeader("Authorization") String token) {
        log.debug("GET /api/notes");
        return ResponseEntity.ok(noteService.getAllNotes(token));
    }

    @GetMapping("/archived")
    public ResponseEntity<List<NoteResponseDto>> getArchivedNotes(@RequestHeader("Authorization") String token) {
        log.debug("GET /api/notes/archived");
        return ResponseEntity.ok(noteService.getArchivedNotes(token));
    }

    @GetMapping("/trash")
    public ResponseEntity<List<NoteResponseDto>> getTrashedNotes(@RequestHeader("Authorization") String token) {
        log.debug("GET /api/notes/trash");
        return ResponseEntity.ok(noteService.getTrashedNotes(token));
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteResponseDto>> searchNotes(@RequestParam String keyword, @RequestHeader("Authorization") String token) {
        log.debug("GET /api/notes/search?keyword={}", keyword);
        return ResponseEntity.ok(noteService.searchNotes(keyword, token));
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<NoteResponseDto> updateNote(@PathVariable Long noteId, @Valid @RequestBody NoteRequestDto requestDto,
                                                      @RequestHeader("Authorization") String token) {
        log.debug("PUT /api/notes/{}", noteId);
        return ResponseEntity.ok(noteService.updateNote(noteId, requestDto, token));
    }

    // permanent delete, only allowed for notes that are in trash
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        log.debug("DELETE /api/notes/{}", noteId);
        noteService.deleteNote(noteId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{noteId}/labels/{labelId}")
    public ResponseEntity<NoteResponseDto> addLabel(@PathVariable Long noteId, @PathVariable Long labelId,
                                                    @RequestHeader("Authorization") String token) {
        log.debug("POST /api/notes/{}/labels/{}", noteId, labelId);
        return ResponseEntity.ok(labelService.addLabelToNote(noteId, labelId, token));
    }

    @DeleteMapping("/{noteId}/labels/{labelId}")
    public ResponseEntity<NoteResponseDto> removeLabel(@PathVariable Long noteId, @PathVariable Long labelId,
                                                       @RequestHeader("Authorization") String token) {
        log.debug("DELETE /api/notes/{}/labels/{}", noteId, labelId);
        return ResponseEntity.ok(labelService.removeLabelFromNote(noteId, labelId, token));
    }

    // pin, archive and trash toggle the current state of the note
    @PatchMapping("/{noteId}/pin")
    public ResponseEntity<NoteResponseDto> pinNote(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        log.debug("PATCH /api/notes/{}/pin", noteId);
        return ResponseEntity.ok(noteService.pinNote(noteId, token));
    }

    @PatchMapping("/{noteId}/archive")
    public ResponseEntity<NoteResponseDto> archiveNote(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        log.debug("PATCH /api/notes/{}/archive", noteId);
        return ResponseEntity.ok(noteService.archiveNote(noteId, token));
    }

    @PatchMapping("/{noteId}/trash")
    public ResponseEntity<NoteResponseDto> trashNote(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        log.debug("PATCH /api/notes/{}/trash", noteId);
        return ResponseEntity.ok(noteService.trashNote(noteId, token));
    }
}
