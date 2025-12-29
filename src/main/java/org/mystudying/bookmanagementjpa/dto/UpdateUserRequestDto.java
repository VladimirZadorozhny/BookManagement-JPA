package org.mystudying.bookmanagementjpa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequestDto(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email
) {
}

