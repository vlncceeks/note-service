package app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoteRequest(
    @NotBlank @Size(max = 255) String title,
    @NotNull @Size(max = 4000) String content) {
}
