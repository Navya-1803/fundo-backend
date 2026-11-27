package com.fundoo.notes.exception;

/**
 * Thrown when a label does not exist or belongs to another user.
 *
 * @author Navya Raj
 */
public class LabelNotFoundException extends RuntimeException {

    public LabelNotFoundException(Long labelId) {
        super("Label not found with id: " + labelId);
    }
}
