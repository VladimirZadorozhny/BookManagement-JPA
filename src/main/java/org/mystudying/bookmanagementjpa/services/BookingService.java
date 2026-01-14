package org.mystudying.bookmanagementjpa.services;

import org.mystudying.bookmanagementjpa.domain.Booking;
import org.mystudying.bookmanagementjpa.dto.BookingReportDto;
import org.mystudying.bookmanagementjpa.dto.BookingReportType;
import org.mystudying.bookmanagementjpa.repositories.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Page<BookingReportDto> getBookingReport(BookingReportType type, Integer dueSoonDays, Long minActiveBooks, Pageable pageable) {
        Page<Booking> bookings;
        LocalDate now = LocalDate.now();

        switch (type) {
            case ALL:
                bookings = bookingRepository.findAllWithDetails(pageable);
                break;
            case ACTIVE:
                bookings = bookingRepository.findActiveWithDetails(pageable);
                break;
            case RETURNED:
                bookings = bookingRepository.findReturnedWithDetails(pageable);
                break;
            case FINES:
                bookings = bookingRepository.findWithActualOrPotentialFines(now, pageable);
                break;
            case UNPAID_FINES:
                bookings = bookingRepository.findWithUnpaidActualOrPotentialFines(now, pageable);
                break;
            case DUE_SOON:
                LocalDate futureDate = now.plusDays(Objects.requireNonNullElse(dueSoonDays, 3));
                bookings = bookingRepository.findDueSoonWithDetails(now, futureDate, pageable);
                break;
            case HEAVY_USERS:
                bookings = bookingRepository.findBookingsForHeavyUsers(Objects.requireNonNullElse(minActiveBooks, 2L), pageable);
                break;
            default:
                throw new IllegalArgumentException("Unknown report type: " + type);
        }
        return bookings.map(this::toReportDto);
    }

    private BookingReportDto toReportDto(Booking booking) {
        BigDecimal fine = booking.getFine();
        if (booking.getReturnedAt() == null && booking.isExpired()) {
            fine = booking.calculateFine();
        }

        return new BookingReportDto(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getName(),
                booking.getUser().getEmail(),
                booking.getBook().getId(),
                booking.getBook().getTitle(),
                booking.getBorrowedAt(),
                booking.getDueAt(),
                booking.getReturnedAt(),
                booking.overdueDays(),
                fine,
                booking.isFinePaid()
        );
    }
}
