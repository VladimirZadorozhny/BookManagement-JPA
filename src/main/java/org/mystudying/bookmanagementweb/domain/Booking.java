package org.mystudying.bookmanagementweb.domain;

public class Booking {
    private final long userId;
    private final long bookId;

    public Booking(long userId, long bookId) {
        if (userId <= 0 || bookId <= 0) {
            throw new IllegalArgumentException("userId and bookId must be greater than 0.");
        }
        this.userId = userId;
        this.bookId = bookId;
    }

    public long getUserId() {
        return userId;
    }

    public long getBookId() {
        return bookId;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "userId=" + userId +
                ", bookId=" + bookId +
                '}';
    }
}

