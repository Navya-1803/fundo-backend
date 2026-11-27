package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.service.LabelService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints to manage labels and list the notes under a label.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/labels")
public class LabelController {

    private static final Logger log = LoggerFactory.getLogger(LabelController.class);

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @PostMapping
    public ResponseEntity<LabelResponseDto> createLabel(@Valid @RequestBody LabelRequestDto requestDto,
                                                        @RequestHeader("Authorization") String token) {
        log.debug("POST /api/labels");
        return ResponseEntity.status(HttpStatus.CREATED).body(labelService.createLabel(requestDto, token));
    }

    @GetMapping
    public ResponseEntity<List<LabelResponseDto>> getLabels(@RequestHeader("Authorization") String token) {
        log.debug("GET /api/labels");
        return ResponseEntity.ok(labelService.getLabels(token));
    }

    @PutMapping("/{labelId}")
    public ResponseEntity<LabelResponseDto> renameLabel(@PathVariable Long labelId, @Valid @RequestBody LabelRequestDto requestDto,
                                                        @RequestHeader("Authorization") String token) {
        log.debug("PUT /api/labels/{}", labelId);
        return ResponseEntity.ok(labelService.renameLabel(labelId, requestDto, token));
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long labelId, @RequestHeader("Authorization") String token) {
        log.debug("DELETE /api/labels/{}", labelId);
        labelService.deleteLabel(labelId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{labelId}/notes")
    public ResponseEntity<List<NoteResponseDto>> getNotesByLabel(@PathVariable Long labelId, @RequestHeader("Authorization") String token) {
        log.debug("GET /api/labels/{}/notes", labelId);
        return ResponseEntity.ok(labelService.getNotesByLabel(labelId, token));
    }
}
