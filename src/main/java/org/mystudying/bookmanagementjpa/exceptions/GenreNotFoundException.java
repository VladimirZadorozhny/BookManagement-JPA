package org.mystudying.bookmanagementjpa.exceptions;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException(long id) {
        super("Genre not found with id: " + id);
    }

    public GenreNotFoundException(String name) {
        super("Genre not found with name: " + name);
    }
}
