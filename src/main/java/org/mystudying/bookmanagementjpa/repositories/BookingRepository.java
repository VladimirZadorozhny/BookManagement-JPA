package org.mystudying.bookmanagementjpa.repositories;

import org.mystudying.bookmanagementjpa.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserId(Long userId);
    
    List<Booking> findByBookId(Long bookId);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.book.id = :bookId AND b.returnedAt IS NULL")
    Optional<Booking> findActiveBooking(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Query("SELECT b FROM Booking b WHERE b.returnedAt IS NULL AND b.dueAt < :date")
    List<Booking> findOverdueBookings(@Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.returnedAt IS NULL AND b.dueAt BETWEEN :now AND :futureDate")
    List<Booking> findBookingsDueSoon(@Param("now") LocalDate now, @Param("futureDate") LocalDate futureDate);
    
    @Query("SELECT b FROM Booking b WHERE b.fine > 0")
    List<Booking> findBookingsWithFines();
}
