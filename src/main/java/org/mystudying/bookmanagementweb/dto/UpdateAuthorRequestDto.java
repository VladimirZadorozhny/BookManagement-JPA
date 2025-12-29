package org.mystudying.bookmanagementweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record UpdateAuthorRequestDto(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotNull(message = "Birthdate cannot be null")
        @PastOrPresent(message = "Birthdate cannot be in the future")
        LocalDate birthdate
) {
}

