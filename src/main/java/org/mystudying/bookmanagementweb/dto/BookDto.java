package org.mystudying.bookmanagementweb.dto;

public record BookDto(
        long id,
        String title,
        int year,
        long authorId,
        int available
) {
}

