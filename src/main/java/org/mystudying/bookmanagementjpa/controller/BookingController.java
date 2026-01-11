package org.mystudying.bookmanagementjpa.controller;

import org.mystudying.bookmanagementjpa.dto.BookingResponseDto;
import org.mystudying.bookmanagementjpa.services.BookingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingResponseDto> getAllBookings() {
        return bookingService.findAll();
    }

    @GetMapping("/overdue")
    public List<BookingResponseDto> getOverdueBookings() {
        return bookingService.findOverdue();
    }

    @GetMapping("/due-soon")
    public List<BookingResponseDto> getBookingsDueSoon() {
        return bookingService.findDueSoon();
    }

    @GetMapping("/fines")
    public List<BookingResponseDto> getBookingsWithFines() {
        return bookingService.findWithFines();
    }
}
