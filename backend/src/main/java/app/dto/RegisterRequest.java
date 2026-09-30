package app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_.-]*$", message = "only letters, digits, '_', '.', '-' are allowed")
    String username,

    @NotBlank
    @Size(min = 6, max = 72)
    @Pattern(regexp = "^[A-Za-z0-9_.-]*$", message = "only letters, digits, '_', '.', '-' are allowed")
    String password) {
}
