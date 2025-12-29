package org.mystudying.bookmanagementweb.exceptions;

public class BookNotAvailableException extends RuntimeException {
    public BookNotAvailableException(long bookId) {
        super("Book with id '" + bookId + "' is not available.");
    }
}

