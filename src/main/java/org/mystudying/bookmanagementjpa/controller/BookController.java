package org.mystudying.bookmanagementjpa.controller;

import jakarta.validation.Valid;
import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.dto.BookDetailDto;
import org.mystudying.bookmanagementjpa.dto.BookDto;
import org.mystudying.bookmanagementjpa.dto.CreateBookRequestDto;
import org.mystudying.bookmanagementjpa.dto.UpdateBookRequestDto;
import org.mystudying.bookmanagementjpa.exceptions.BookNotFoundException;
import org.mystudying.bookmanagementjpa.services.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookDto> getAllBooks(@RequestParam Optional<Boolean> available,
                                     @RequestParam Optional<Integer> year,
                                     @RequestParam Optional<String> authorName,
                                     @RequestParam Optional<String> title,
                                     @RequestParam Optional<String> authorPartName) {
        if (available.isPresent()) {
            return bookService.findByAvailability(available.get()).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        if (year.isPresent()) {
            return bookService.findByYear(year.get()).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        if (title.isPresent()) {
            return bookService.findByTitleContaining(title.get()).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        if (authorPartName.isPresent()) {
            return bookService.findByAuthorNameContaining(authorPartName.get()).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        if (authorName.isPresent()) {
            return bookService.findByAuthorName(authorName.get()).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        return bookService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public BookDto getBookById(@PathVariable long id) {
        return bookService.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @GetMapping("/{id}/details")
    public BookDetailDto getBookDetailsById(@PathVariable long id) {
        return bookService.findBookDetailsById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @GetMapping("/title/{title}")
    public BookDto getBookByTitle(@PathVariable String title) {
        return bookService.findByTitle(title)
                .map(this::toDto)
                .orElseThrow(() -> new BookNotFoundException(title));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto createBook(@Valid @RequestBody CreateBookRequestDto bookDto) {
        Book book = new Book(1L, bookDto.title(), bookDto.year(), bookDto.authorId(), bookDto.available());
        long newId = bookService.save(book);

        return toDto(new Book(newId, book.getTitle(), book.getYear(), book.getAuthorId(), book.getAvailable()));
    }

    @PutMapping("/{id}")
    public BookDto updateBook(@PathVariable long id, @Valid @RequestBody UpdateBookRequestDto bookDto) {
        bookService.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        Book bookToUpdate = new Book(id, bookDto.title(), bookDto.year(), bookDto.authorId(), bookDto.available());
        bookService.update(bookToUpdate);
        return toDto(bookToUpdate);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable long id) {
        bookService.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        bookService.deleteById(id);
    }

    private BookDto toDto(Book book) {
        return new BookDto(book.getId(), book.getTitle(), book.getYear(), book.getAuthorId(), book.getAvailable());
    }
}

