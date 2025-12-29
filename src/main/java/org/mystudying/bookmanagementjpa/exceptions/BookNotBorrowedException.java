package org.mystudying.bookmanagementjpa.exceptions;

public class BookNotBorrowedException extends RuntimeException {
    public BookNotBorrowedException() {
        super("User does not have this book.");
    }
}

