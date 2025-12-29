package org.mystudying.bookmanagementjpa.services;


import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.dto.BookDetailDto;
import org.mystudying.bookmanagementjpa.exceptions.AuthorNotFoundException;
import org.mystudying.bookmanagementjpa.exceptions.BookHasBookingsException;
import org.mystudying.bookmanagementjpa.repositories.AuthorRepository;
import org.mystudying.bookmanagementjpa.repositories.BookRepository;
import org.mystudying.bookmanagementjpa.repositories.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final BookingRepository bookingRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, BookingRepository bookingRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.bookingRepository = bookingRepository;
        this.authorRepository = authorRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public List<Book> findByYear(int year) {
        return bookRepository.findByYear(year);
    }

    public List<Book> findByAuthorName(String authorName) {
        return bookRepository.findByAuthorName(authorName);
    }

    public List<Book> findByAuthorId(long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }

    public List<Book> findByAvailability(boolean available) {
        return bookRepository.findByAvailability(available);
    }

    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    public Optional<BookDetailDto> findBookDetailsById(long id) {
        return bookRepository.findBookDetailsById(id);
    }

    public Optional<Book> findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    public List<Book> findByTitleContaining(String title) {
        return bookRepository.findByTitleContaining(title);
    }

    public List<Book> findByAuthorNameContaining(String authorName) {
        return bookRepository.findByAuthorNameContaining(authorName);
    }

    @Transactional
    public long save(Book book) {
        authorRepository.findById(book.getAuthorId())
                .orElseThrow(() -> new AuthorNotFoundException(book.getAuthorId()));
        return bookRepository.save(book);
    }

    @Transactional
    public void update(Book book) {
        authorRepository.findById(book.getAuthorId())
                .orElseThrow(() -> new AuthorNotFoundException(book.getAuthorId()));
        bookRepository.update(book);
    }

    @Transactional
    public void deleteById(long id) {
        if (bookingRepository.existsByBookId(id)) {
            throw new BookHasBookingsException(id);
        }
        bookRepository.deleteById(id);
    }
}

