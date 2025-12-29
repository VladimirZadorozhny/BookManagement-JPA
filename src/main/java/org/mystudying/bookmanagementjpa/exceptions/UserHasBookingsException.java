package org.mystudying.bookmanagementjpa.exceptions;

public class UserHasBookingsException extends RuntimeException {
    public UserHasBookingsException(long userId) {
        super("Cannot delete user with ID '" + userId + "' because they have active bookings.");
    }
}

