package org.mystudying.bookmanagementjpa.controller;

import org.mystudying.bookmanagementjpa.dto.BookingReportDto;
import org.mystudying.bookmanagementjpa.dto.BookingReportType;
import org.mystudying.bookmanagementjpa.services.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public Page<BookingReportDto> getBookingReport(
            @RequestParam(name = "type") BookingReportType type,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Long minActiveBooks,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return bookingService.getBookingReport(type, days, minActiveBooks, pageable);
    }
}
