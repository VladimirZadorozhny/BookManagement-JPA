package org.mystudying.bookmanagementjpa.exceptions;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(long id) {
        super("Book not found. Id: " + id);
    }
    public BookNotFoundException(String title) {
        super("Book not found. Title: " + title);
    }
}

