package org.mystudying.bookmanagementjpa.services;

import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.domain.Booking;
import org.mystudying.bookmanagementjpa.domain.User;
import org.mystudying.bookmanagementjpa.dto.BookingResponseDto;
import org.mystudying.bookmanagementjpa.dto.CreateUserRequestDto;
import org.mystudying.bookmanagementjpa.dto.UpdateUserRequestDto;
import org.mystudying.bookmanagementjpa.exceptions.*;
import org.mystudying.bookmanagementjpa.repositories.BookRepository;
import org.mystudying.bookmanagementjpa.repositories.BookingRepository;
import org.mystudying.bookmanagementjpa.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookingRepository bookingRepository;

    public UserService(UserRepository userRepository, BookRepository bookRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll(Sort.by("name"));
    }

    public List<User> findUsersWithMoreThanXBooks(long count) {
        return userRepository.findUsersWithMoreThanXBooks(count);
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * @deprecated Since introduction of Booking entity.
     * Use BookingRepository instead.
     * Kept temporarily to avoid breaking existing tests.
     */
    @Deprecated
    public List<Book> findBooksByUserId(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return bookingRepository.findActiveBookingsWithBooksByUserId(userId).stream()
                .map(Booking::getBook)
                .collect(Collectors.toList());
    }

    public List<BookingResponseDto> findBookingsByUserId(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return bookingRepository.findAllByUserIdWithBooks(userId).stream()
                .sorted((b1, b2) -> {
                    if (b1.getReturnedAt() == null && b2.getReturnedAt() != null) return -1;
                    if (b1.getReturnedAt() != null && b2.getReturnedAt() == null) return 1;
                    return b2.getBorrowedAt().compareTo(b1.getBorrowedAt());
                })
                .map(b -> {
                    BigDecimal displayFine = b.getFine();
                    if (b.getReturnedAt() == null && b.isExpired()) {
                        displayFine = b.calculateFine();
                    }
                    return new BookingResponseDto(
                            b.getId(),
                            user.getId(),
                            user.getName(),
                            b.getBook().getId(),
                            b.getBook().getTitle(),
                            b.getBook().getYear(),
                            b.getBorrowedAt(),
                            b.getDueAt(),
                            b.getReturnedAt(),
                            displayFine,
                            b.isFinePaid()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void payFine(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookNotBorrowedException());
        
        if (booking.getUser().getId() != userId) {
             throw new UserNotFoundException(userId); // Mismatch
        }
        
        if (booking.getFine().compareTo(BigDecimal.ZERO) > 0 && !booking.isFinePaid()) {
            booking.setFinePaid(true);
        }
    }

    @Transactional
    public User save(CreateUserRequestDto createUserRequestDto) {
        try {
            return userRepository.save(new User(null, createUserRequestDto.name(), createUserRequestDto.email()));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(createUserRequestDto.email());
        }
    }

    @Transactional
    public User update(long id, UpdateUserRequestDto updateUserRequestDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        try {
            user.setName(updateUserRequestDto.name());
            user.setEmail(updateUserRequestDto.email());
            userRepository.saveAndFlush(user);
            return user;
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
    }

    @Transactional
    public void deleteById(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (!user.getBookings().isEmpty()) {
            throw new UserHasBookingsException(id);
        }
        userRepository.delete(user);
    }

    @Transactional
    public void rentBook(long userId, long bookId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Book book = bookRepository.findAndLockById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        if (bookingRepository.findActiveBooking(userId, bookId).isPresent()) {
            throw new BookAlreadyBorrowedException();
        }

        // Extra checking before renting
        boolean hasOverdue = user.getBookings().stream()
                .anyMatch(Booking::isExpired);
        if (hasOverdue) {
            throw new UserHasOverdueBooksException(userId);
        }
        
        boolean hasFines = user.getBookings().stream()
                .anyMatch(b -> b.getFine().compareTo(BigDecimal.ZERO) > 0 && !b.isFinePaid());
        if (hasFines) {
             throw new UserHasUnpaidFinesException(userId);
        }

        book.rentBook();
        Booking booking = new Booking(user, book, LocalDate.now(), LocalDate.now().plusDays(14));
        user.addBooking(booking);
        bookingRepository.save(booking);
    }

    @Transactional
    public void returnBook(long userId, long bookId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        Booking booking = bookingRepository.findActiveBooking(userId, bookId)
                .orElseThrow(() -> new BookNotBorrowedException());

        booking.setReturnedAt(LocalDate.now());
        booking.setFine(booking.calculateFine());

        booking.getBook().returnBook();
    }
}
