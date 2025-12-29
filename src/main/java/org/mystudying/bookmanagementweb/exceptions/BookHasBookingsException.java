package org.mystudying.bookmanagementweb.exceptions;

public class BookHasBookingsException extends RuntimeException {
    public BookHasBookingsException(long bookId) {
        super("Cannot delete book with ID '" + bookId + "' because it has existing bookings.");
    }
}

