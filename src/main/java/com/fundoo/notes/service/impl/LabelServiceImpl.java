package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.LabelNotFoundException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.LabelRepository;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.LabelService;
import com.fundoo.notes.service.TokenService;
import com.fundoo.notes.util.NoteMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates, renames and deletes labels and attaches them to notes. Every
 * change that is visible on the dashboard evicts the user's notes cache.
 *
 * @author Navya Raj
 */
@Service
@Transactional
public class LabelServiceImpl implements LabelService {

    private static final Logger log = LoggerFactory.getLogger(LabelServiceImpl.class);

    private static final String USER_KEY = "@jwtTokenService.extractUserId(#token)";

    private final LabelRepository labelRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public LabelServiceImpl(LabelRepository labelRepository, NoteRepository noteRepository,
                            UserRepository userRepository, TokenService tokenService) {
        this.labelRepository = labelRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    public LabelResponseDto createLabel(LabelRequestDto requestDto, String token) {
        User user = findUser(token);
        String name = requestDto.getName().trim();
        ensureNameIsFree(user.getId(), name);
        Label label = labelRepository.save(new Label(name, user));
        log.info("Label '{}' created with id: {} for user id: {}", name, label.getId(), user.getId());
        return NoteMapper.toResponse(label);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelResponseDto> getLabels(String token) {
        Long userId = tokenService.extractUserId(token);
        return labelRepository.findByUserIdOrderByNameAsc(userId).stream().map(NoteMapper::toResponse).toList();
    }

    @Override
    @CacheEvict(value = NoteServiceImpl.NOTES_CACHE, key = USER_KEY)
    public LabelResponseDto renameLabel(Long labelId, LabelRequestDto requestDto, String token) {
        Long userId = tokenService.extractUserId(token);
        Label label = findOwnedLabel(labelId, userId);
        String name = requestDto.getName().trim();
        if (!label.getName().equalsIgnoreCase(name)) {
            ensureNameIsFree(userId, name);
        }
        label.setName(name);
        log.info("Label id: {} renamed to '{}'", labelId, name);
        return NoteMapper.toResponse(labelRepository.save(label));
    }

    @Override
    @CacheEvict(value = NoteServiceImpl.NOTES_CACHE, key = USER_KEY)
    public void deleteLabel(Long labelId, String token) {
        Label label = findOwnedLabel(labelId, tokenService.extractUserId(token));
        // the note side owns the relation, so the label is removed from its notes first
        List<Note> notes = noteRepository.findByLabelsId(labelId);
        notes.forEach(note -> note.removeLabel(label));
        noteRepository.saveAll(notes);
        labelRepository.delete(label);
        log.info("Label id: {} deleted and removed from {} note(s)", labelId, notes.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getNotesByLabel(Long labelId, String token) {
        Long userId = tokenService.extractUserId(token);
        findOwnedLabel(labelId, userId);
        return noteRepository.findByUserIdAndLabelsIdAndTrashedFalse(userId, labelId).stream()
                .map(NoteMapper::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(value = NoteServiceImpl.NOTES_CACHE, key = USER_KEY)
    public NoteResponseDto addLabelToNote(Long noteId, Long labelId, String token) {
        Long userId = tokenService.extractUserId(token);
        Note note = findOwnedNote(noteId, userId);
        note.addLabel(findOwnedLabel(labelId, userId));
        log.info("Label id: {} added to note id: {}", labelId, noteId);
        return NoteMapper.toResponse(noteRepository.save(note));
    }

    @Override
    @CacheEvict(value = NoteServiceImpl.NOTES_CACHE, key = USER_KEY)
    public NoteResponseDto removeLabelFromNote(Long noteId, Long labelId, String token) {
        Long userId = tokenService.extractUserId(token);
        Note note = findOwnedNote(noteId, userId);
        note.removeLabel(findOwnedLabel(labelId, userId));
        log.info("Label id: {} removed from note id: {}", labelId, noteId);
        return NoteMapper.toResponse(noteRepository.save(note));
    }

    private void ensureNameIsFree(Long userId, String name) {
        if (labelRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            log.warn("Label '{}' already exists for user id: {}", name, userId);
            throw new IllegalArgumentException("Label already exists: " + name);
        }
    }

    private User findUser(String token) {
        return userRepository.findById(tokenService.extractUserId(token))
                .orElseThrow(() -> new InvalidCredentialsException("User of this token no longer exists"));
    }

    private Label findOwnedLabel(Long labelId, Long userId) {
        return labelRepository.findByIdAndUserId(labelId, userId).orElseThrow(() -> new LabelNotFoundException(labelId));
    }

    private Note findOwnedNote(Long noteId, Long userId) {
        return noteRepository.findByIdAndUserId(noteId, userId).orElseThrow(() -> new NoteNotFoundException(noteId));
    }
}
