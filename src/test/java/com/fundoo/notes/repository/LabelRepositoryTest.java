package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LabelRepositoryTest {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User navya;
    private User other;
    private Label work;

    @BeforeEach
    void setUp() {
        navya = userRepository.save(new User("Navya", "Raj", "navya@example.com", "hash"));
        other = userRepository.save(new User("Other", "User", "other@example.com", "hash"));
        work = labelRepository.save(new Label("Work", navya));
        labelRepository.save(new Label("Home", navya));
        labelRepository.save(new Label("Work", other));
    }

    @Test
    void testFindByUser_SortedByName() {
        assertEquals(java.util.List.of("Home", "Work"),
                labelRepository.findByUserIdOrderByNameAsc(navya.getId()).stream().map(Label::getName).toList());
    }

    @Test
    void testOwnershipAndNameChecks() {
        assertTrue(labelRepository.findByIdAndUserId(work.getId(), navya.getId()).isPresent());
        assertTrue(labelRepository.findByIdAndUserId(work.getId(), other.getId()).isEmpty());
        assertTrue(labelRepository.existsByUserIdAndNameIgnoreCase(navya.getId(), "WORK"));
        assertFalse(labelRepository.existsByUserIdAndNameIgnoreCase(navya.getId(), "Travel"));
    }

    @Test
    void testSameNameTwiceForOneUser_Rejected() {
        assertThrows(DataIntegrityViolationException.class, () -> labelRepository.saveAndFlush(new Label("Work", navya)));
    }

    @Test
    void testNotesByLabel() {
        Note labelled = new Note("Report", "", navya);
        labelled.addLabel(work);
        noteRepository.save(labelled);
        Note trashed = new Note("Old report", "", navya);
        trashed.addLabel(work);
        trashed.setTrashed(true);
        noteRepository.save(trashed);
        noteRepository.save(new Note("Unlabelled", "", navya));

        assertEquals(1, noteRepository.findByUserIdAndLabelsIdAndTrashedFalse(navya.getId(), work.getId()).size());
        assertEquals(2, noteRepository.findByLabelsId(work.getId()).size());
    }
}
