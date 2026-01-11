package org.mystudying.bookmanagementjpa.exceptions;

public class UserHasUnpaidFinesException extends RuntimeException {
    public UserHasUnpaidFinesException(long userId) {
        super("User with id " + userId + " has unpaid fines!");
    }
}
