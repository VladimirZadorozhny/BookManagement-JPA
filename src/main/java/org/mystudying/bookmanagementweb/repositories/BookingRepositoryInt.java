package org.mystudying.bookmanagementweb.repositories;

import org.mystudying.bookmanagementweb.domain.Booking;

import java.util.Optional;

public interface BookingRepositoryInt {
    void create(Booking booking);
    void delete(Booking booking);
    Optional<Booking> find(long userId, long bookId);
    boolean existsByBookId(long bookId);
    boolean existsByUserId(long userId);
}

