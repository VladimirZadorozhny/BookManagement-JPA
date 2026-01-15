package org.mystudying.bookmanagementjpa.dto;

public record BookDto(
        long id,
        String title,
        int year,
        int available
) {
}

