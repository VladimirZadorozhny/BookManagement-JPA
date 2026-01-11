package org.mystudying.bookmanagementjpa.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDate borrowedAt;

    @Column(name = "due_at", nullable = false)
    private LocalDate dueAt;

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @Column(name = "fine")
    private BigDecimal fine = BigDecimal.ZERO;

    @Column(name = "fine_paid")
    private boolean finePaid = false;

    protected Booking() {
        // Required by JPA
    }

    public Booking(User user, Book book, LocalDate borrowedAt, LocalDate dueAt) {
        this.user = user;
        this.book = book;
        this.borrowedAt = borrowedAt;
        this.dueAt = dueAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowedAt() {
        return borrowedAt;
    }

    public LocalDate getDueAt() {
        return dueAt;
    }

    public LocalDate getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDate returnedAt) {
        this.returnedAt = returnedAt;
    }

    public BigDecimal getFine() {
        return fine;
    }

    public void setFine(BigDecimal fine) {
        this.fine = fine;
    }

    public boolean isFinePaid() {
        return finePaid;
    }

    public void setFinePaid(boolean finePaid) {
        this.finePaid = finePaid;
    }

    public boolean isExpired() {
        return returnedAt == null && LocalDate.now().isAfter(dueAt);
    }

    public long overdueDays() {
        if (returnedAt != null) {
             if (returnedAt.isAfter(dueAt)) {
                 return ChronoUnit.DAYS.between(dueAt, returnedAt);
             }
             return 0;
        }
        if (!isExpired()) return 0;
        return ChronoUnit.DAYS.between(dueAt, LocalDate.now());
    }

    public BigDecimal calculateFine() {
        return BigDecimal.valueOf(overdueDays()); // $1 per day assumption
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}