package com.fundoo.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body used to create or rename a label.
 *
 * @author Navya Raj
 */
public class LabelRequestDto {

    @NotBlank(message = "Label name is required")
    @Size(max = 50, message = "Label name must be at most 50 characters")
    private String name;

    public LabelRequestDto() {
    }

    public LabelRequestDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
