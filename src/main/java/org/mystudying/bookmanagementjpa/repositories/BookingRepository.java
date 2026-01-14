package org.mystudying.bookmanagementjpa.repositories;

import org.mystudying.bookmanagementjpa.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    @Query("SELECT b FROM Booking b JOIN FETCH b.book WHERE b.user.id = :userId")
    List<Booking> findAllByUserIdWithBooks(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.book WHERE b.user.id = :userId AND b.returnedAt IS NULL")
    List<Booking> findActiveBookingsWithBooksByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.book WHERE b.user.id = :userId AND b.book.id = :bookId AND b.returnedAt IS NULL")
    Optional<Booking> findActiveBooking(@Param("userId") Long userId, @Param("bookId") Long bookId);

    // --- Reports with Pagination and Eager Fetching ---

    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.book",
           countQuery = "SELECT COUNT(b) FROM Booking b")
    Page<Booking> findAllWithDetails(Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.book WHERE b.returnedAt IS NULL",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.returnedAt IS NULL")
    Page<Booking> findActiveWithDetails(Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.book WHERE b.returnedAt IS NOT NULL",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.returnedAt IS NOT NULL")
    Page<Booking> findReturnedWithDetails(Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.book WHERE b.fine > 0 OR (b.returnedAt IS NULL AND b.dueAt < :now)",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.fine > 0 OR (b.returnedAt IS NULL AND b.dueAt < :now)")
    Page<Booking> findWithActualOrPotentialFines(@Param("now") LocalDate now, Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.book WHERE (b.fine > 0 AND b.finePaid = false) OR (b.returnedAt IS NULL AND b.dueAt < :now)",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE (b.fine > 0 AND b.finePaid = false) OR (b.returnedAt IS NULL AND b.dueAt < :now)")
    Page<Booking> findWithUnpaidActualOrPotentialFines(@Param("now") LocalDate now, Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.book WHERE b.returnedAt IS NULL AND b.dueAt < :date",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.returnedAt IS NULL AND b.dueAt < :date")
    Page<Booking> findOverdueWithDetails(@Param("date") LocalDate date, Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.book WHERE b.returnedAt IS NULL AND b.dueAt BETWEEN :now AND :futureDate",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.returnedAt IS NULL AND b.dueAt BETWEEN :now AND :futureDate")
    Page<Booking> findDueSoonWithDetails(@Param("now") LocalDate now, @Param("futureDate") LocalDate futureDate, Pageable pageable);

    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.book WHERE b.user.id IN " +
                   "(SELECT b2.user.id FROM Booking b2 WHERE b2.returnedAt IS NULL GROUP BY b2.user.id HAVING COUNT(b2) > :count)",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.id IN " +
                        "(SELECT b2.user.id FROM Booking b2 WHERE b2.returnedAt IS NULL GROUP BY b2.user.id HAVING COUNT(b2) > :count)")
    Page<Booking> findBookingsForHeavyUsers(@Param("count") Long count, Pageable pageable);
}
