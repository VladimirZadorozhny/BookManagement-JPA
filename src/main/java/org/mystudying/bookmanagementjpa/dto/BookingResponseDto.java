package org.mystudying.bookmanagementjpa.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponseDto(
        long id,
        long userId,
        String userName,
        long bookId,
        String bookTitle,
        int bookYear,
        LocalDate borrowedAt,
        LocalDate dueAt,
        LocalDate returnedAt,
        BigDecimal fine,
        boolean finePaid
) {
}