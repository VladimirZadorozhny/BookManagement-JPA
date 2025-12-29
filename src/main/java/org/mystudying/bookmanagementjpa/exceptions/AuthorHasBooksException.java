package org.mystudying.bookmanagementjpa.exceptions;

public class AuthorHasBooksException extends RuntimeException {
    public AuthorHasBooksException(long authorId) {
        super("Cannot delete author with ID '" + authorId + "' because they have associated books.");
    }
}

