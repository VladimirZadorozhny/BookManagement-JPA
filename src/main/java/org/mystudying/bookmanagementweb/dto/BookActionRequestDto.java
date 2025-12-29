package org.mystudying.bookmanagementweb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookActionRequestDto(
        @NotNull(message = "Book ID cannot be null")
        @Positive(message = "Book ID must be a positive number")
        Long bookId
) {
}

