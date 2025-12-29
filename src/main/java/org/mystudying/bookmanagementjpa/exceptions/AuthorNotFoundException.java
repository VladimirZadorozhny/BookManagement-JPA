package org.mystudying.bookmanagementjpa.exceptions;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(long id) {
        super("Author not found. Id: " + id);
    }
    public AuthorNotFoundException(String name) {
        super("Author not found. Name: " + name);
    }
}

