package com.fundoo.notes.dto.response;

import java.io.Serial;
import java.io.Serializable;

/**
 * Label returned to the client.
 *
 * @author Navya Raj
 */
public class LabelResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;

    public LabelResponseDto() {
    }

    public LabelResponseDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
