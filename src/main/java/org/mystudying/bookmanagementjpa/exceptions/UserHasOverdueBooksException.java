package org.mystudying.bookmanagementjpa.exceptions;

public class UserHasOverdueBooksException extends RuntimeException {
    public UserHasOverdueBooksException(long userId) {
        super("User with id " + userId + " has overdue books!");
    }
}
