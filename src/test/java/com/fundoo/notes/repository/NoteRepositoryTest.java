package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User navya;
    private User other;

    @BeforeEach
    void setUp() {
        navya = userRepository.save(new User("Navya", "Raj", "navya@example.com", "hash"));
        other = userRepository.save(new User("Other", "User", "other@example.com", "hash"));

        noteRepository.save(new Note("Shopping list", "milk", navya));
        Note archived = new Note("Old ideas", "", navya);
        archived.setArchived(true);
        noteRepository.save(archived);
        Note trashed = new Note("Deleted", "", navya);
        trashed.setTrashed(true);
        noteRepository.save(trashed);
        noteRepository.save(new Note("Other user's note", "", other));
    }

    @Test
    void testFindByUserId() {
        assertEquals(3, noteRepository.findByUserId(navya.getId()).size());
        assertEquals(1, noteRepository.findByUserId(other.getId()).size());
    }

    @Test
    void testFindActiveNotes() {
        assertEquals(1, noteRepository.findByUserIdAndArchivedFalseAndTrashedFalse(navya.getId()).size());
    }

    @Test
    void testFindArchivedAndTrashedNotes() {
        assertEquals(1, noteRepository.findByUserIdAndArchivedTrueAndTrashedFalse(navya.getId()).size());
        assertEquals(1, noteRepository.findByUserIdAndTrashedTrue(navya.getId()).size());
    }

    @Test
    void testSearchByTitle_IgnoresCase() {
        assertEquals(1, noteRepository.findByUserIdAndTitleContainingIgnoreCase(navya.getId(), "SHOPPING").size());
        assertTrue(noteRepository.findByUserIdAndTitleContainingIgnoreCase(navya.getId(), "other").isEmpty());
    }

    @Test
    void testFindByIdAndUserId_OnlyOwnerCanSeeNote() {
        Note note = noteRepository.findByUserId(navya.getId()).get(0);
        assertTrue(noteRepository.findByIdAndUserId(note.getId(), navya.getId()).isPresent());
        assertTrue(noteRepository.findByIdAndUserId(note.getId(), other.getId()).isEmpty());
    }

    @Test
    void testFindTrashedNotesBeforeCutoff() {
        assertEquals(1, noteRepository.findByTrashedTrueAndUpdatedAtBefore(java.time.LocalDateTime.now().plusMinutes(1)).size());
        assertTrue(noteRepository.findByTrashedTrueAndUpdatedAtBefore(java.time.LocalDateTime.now().minusDays(1)).isEmpty());
    }

    @Test
    void testFindNotesWithReminders_SortedAndWithoutTrash() {
        java.util.List<Note> notes = noteRepository.findByUserId(navya.getId());
        Note first = notes.get(0);
        first.setReminderTime(java.time.LocalDateTime.of(2030, 5, 1, 9, 0));
        Note second = notes.get(1);
        second.setReminderTime(java.time.LocalDateTime.of(2030, 1, 1, 9, 0));
        Note trashed = notes.get(2);
        trashed.setReminderTime(java.time.LocalDateTime.of(2030, 2, 1, 9, 0));
        noteRepository.saveAll(notes);
        java.util.List<Note> reminders = noteRepository.findByUserIdAndReminderTimeIsNotNullAndTrashedFalseOrderByReminderTimeAsc(navya.getId());
        assertEquals(2, reminders.size());
        assertEquals(second.getId(), reminders.get(0).getId());
    }

    @Test
    void testFindDueReminders() {
        java.util.List<Note> notes = noteRepository.findByUserId(navya.getId());
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        notes.get(0).setReminderTime(now.minusMinutes(1));
        notes.get(1).setReminderTime(now.plusDays(1));
        notes.get(2).setReminderTime(now.minusMinutes(5));
        noteRepository.saveAll(notes);
        // the third note is in trash, the second is not due yet
        assertEquals(1, noteRepository.findByReminderTimeLessThanEqualAndReminderSentFalseAndTrashedFalse(now).size());
        notes.get(0).setReminderSent(true);
        noteRepository.save(notes.get(0));
        assertTrue(noteRepository.findByReminderTimeLessThanEqualAndReminderSentFalseAndTrashedFalse(now).isEmpty());
    }
}
