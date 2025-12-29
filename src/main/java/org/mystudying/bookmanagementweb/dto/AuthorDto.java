package org.mystudying.bookmanagementweb.dto;

import java.time.LocalDate;

public record AuthorDto(
        long id,
        String name,
        LocalDate birthdate
) {
}

