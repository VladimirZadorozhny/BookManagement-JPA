package org.mystudying.bookmanagementjpa.services;

import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.dto.BookDetailDto;
import org.mystudying.bookmanagementjpa.dto.CreateBookRequestDto;
import org.mystudying.bookmanagementjpa.dto.UpdateBookRequestDto;
import org.mystudying.bookmanagementjpa.exceptions.AuthorNotFoundException;
import org.mystudying.bookmanagementjpa.exceptions.BookHasBookingsException;
import org.mystudying.bookmanagementjpa.exceptions.BookNotFoundException;
import org.mystudying.bookmanagementjpa.repositories.AuthorRepository;
import org.mystudying.bookmanagementjpa.repositories.BookRepository;
import org.mystudying.bookmanagementjpa.repositories.GenreRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, GenreRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll(Sort.by("title"));
    }

    public List<Book> findByYear(int year) {
        return bookRepository.findByYear(year);
    }

    public List<Book> findByAuthorName(String authorName) {
        return bookRepository.findByAuthorName(authorName);
    }

    public List<Book> findByAuthorId(long authorId) {
        return bookRepository.findByAuthor_Id(authorId);
    }

    public List<Book> findByGenreId(long genreId) {
        return bookRepository.findByGenres_Id(genreId);
    }

    public List<Book> findByAvailability(boolean available) {
        return bookRepository.findByAvailability(available);
    }

    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    public Optional<BookDetailDto> findBookDetailsById(long id) {
        return bookRepository.findBookDetailsById(id)
                .map(dto -> {
                    dto.setGenres(genreRepository.findNamesByBookId(id));
                    return dto;
                });
    }

    public Optional<Book> findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    public List<Book> findByTitleContaining(String title) {
        return bookRepository.findByTitleContainingOrderByTitle(title);
    }

    public List<Book> findByAuthorNameContaining(String authorName) {
        return bookRepository.findByAuthorNameContaining(authorName);
    }

    @Transactional
    public Book save(CreateBookRequestDto createBookRequestDto) {
        // Validation of Author existence
        var author = authorRepository.findById(createBookRequestDto.authorId())
                .orElseThrow(() -> new AuthorNotFoundException(createBookRequestDto.authorId()));
        
       return bookRepository.save(new Book(null, createBookRequestDto.title(), createBookRequestDto.year(),
               author, createBookRequestDto.available()));
    }

    @Transactional
    public Book update(long id, UpdateBookRequestDto updateBookRequestDto) {
        var book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        var author = authorRepository.findById(updateBookRequestDto.authorId()).orElseThrow(() ->
                new AuthorNotFoundException(updateBookRequestDto.authorId()));

        book.setTitle(updateBookRequestDto.title());
        book.setYear(updateBookRequestDto.year());
        book.setAvailable(updateBookRequestDto.available());
        book.setAuthor(author);

        return book;

    }

    @Transactional
    public void deleteById(long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        if (!book.getBookings().isEmpty()) {
            throw new BookHasBookingsException(id);
        }
        bookRepository.delete(book);
    }
}