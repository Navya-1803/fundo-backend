package com.fundoo.notes.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message published to RabbitMQ when the reminder of a note is due. The
 * consumer turns it into an email to the note's owner.
 *
 * @author Navya Raj
 */
public class ReminderEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long noteId;
    private Long userId;
    private String noteTitle;
    private LocalDateTime reminderTime;
    private String message;
    // owner details used for the reminder email
    private String userEmail;
    private String userFirstName;
    private String noteDescription;

    public ReminderEvent() {
    }

    public ReminderEvent(Long noteId, Long userId, String noteTitle, LocalDateTime reminderTime, String message) {
        this.noteId = noteId;
        this.userId = userId;
        this.noteTitle = noteTitle;
        this.reminderTime = reminderTime;
        this.message = message;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getNoteDescription() {
        return noteDescription;
    }

    public void setNoteDescription(String noteDescription) {
        this.noteDescription = noteDescription;
    }

    @Override
    public String toString() {
        return "ReminderEvent{noteId=" + noteId + ", userId=" + userId + ", reminderTime=" + reminderTime + "}";
    }
}
