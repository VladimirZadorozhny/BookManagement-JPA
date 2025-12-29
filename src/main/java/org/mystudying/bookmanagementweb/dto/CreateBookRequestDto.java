package org.mystudying.bookmanagementweb.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateBookRequestDto(
        @NotBlank(message = "Title cannot be blank")
        String title,

        @NotNull(message = "Year cannot be null")
        @Positive(message = "Year must be a positive number")
        @YearValid
        Integer year,

        @NotNull(message = "Author ID cannot be null")
        @Positive(message = "Author ID must be a positive number")
        Long authorId,

        @NotNull(message = "Available count cannot be null")
        @Min(value = 0, message = "Available count cannot be negative")
        Integer available
) {
}

