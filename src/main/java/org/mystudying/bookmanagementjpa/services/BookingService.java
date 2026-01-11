package org.mystudying.bookmanagementjpa.services;

import org.mystudying.bookmanagementjpa.domain.Booking;
import org.mystudying.bookmanagementjpa.dto.BookingResponseDto;
import org.mystudying.bookmanagementjpa.repositories.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<BookingResponseDto> findAll() {
        return bookingRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingResponseDto> findOverdue() {
        return bookingRepository.findOverdueBookings(LocalDate.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingResponseDto> findDueSoon() {
        return bookingRepository.findBookingsDueSoon(LocalDate.now(), LocalDate.now().plusDays(3)).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingResponseDto> findWithFines() {
        return bookingRepository.findBookingsWithFines().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private BookingResponseDto toDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getName(),
                booking.getBook().getId(),
                booking.getBook().getTitle(),
                booking.getBook().getYear(),
                booking.getBorrowedAt(),
                booking.getDueAt(),
                booking.getReturnedAt(),
                booking.getFine(),
                booking.isFinePaid()
        );
    }
}
