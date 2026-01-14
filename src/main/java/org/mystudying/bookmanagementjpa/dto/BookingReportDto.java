package org.mystudying.bookmanagementjpa.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingReportDto(
        Long bookingId,
        Long userId,
        String userName,
        String userEmail,
        Long bookId,
        String bookTitle,
        LocalDate borrowedAt,
        LocalDate dueAt,
        LocalDate returnedAt,
        long overdueDays,
        BigDecimal fine,
        boolean finePaid
) {}
