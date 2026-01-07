package org.mystudying.bookmanagementjpa.controller;

import jakarta.validation.Valid;
import org.mystudying.bookmanagementjpa.domain.Author;
import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.dto.AuthorDto;
import org.mystudying.bookmanagementjpa.dto.BookDto;
import org.mystudying.bookmanagementjpa.dto.CreateAuthorRequestDto;
import org.mystudying.bookmanagementjpa.dto.UpdateAuthorRequestDto;
import org.mystudying.bookmanagementjpa.exceptions.AuthorNotFoundException;
import org.mystudying.bookmanagementjpa.services.AuthorService;
import org.mystudying.bookmanagementjpa.services.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;
    private final BookService bookService;

    public AuthorController(AuthorService authorService, BookService bookService) {
        this.authorService = authorService;
        this.bookService = bookService;
    }

    @GetMapping
    public List<AuthorDto> getAllAuthors() {
        return authorService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AuthorDto getAuthorById(@PathVariable long id) {
        return authorService.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new AuthorNotFoundException(id));
    }

    @GetMapping("/name/{name}")
    public AuthorDto getAuthorByName(@PathVariable String name) {
        return authorService.findByName(name)
                .map(this::toDto)
                .orElseThrow(() -> new AuthorNotFoundException(name));
    }

    @GetMapping("/{id}/books")
    public List<BookDto> getBooksByAuthor(@PathVariable long id) {
        authorService.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        return bookService.findByAuthorId(id).stream()
                .map(this::toBookDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDto createAuthor(@Valid @RequestBody CreateAuthorRequestDto authorDto) {
        var author = authorService.save(authorDto);
        return toDto(author);
    }

    @PutMapping("/{id}")
    public AuthorDto updateAuthor(@PathVariable long id, @Valid @RequestBody UpdateAuthorRequestDto authorDto) {
        Author authorToUpdate = authorService.update(id, authorDto);
        return toDto(authorToUpdate);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable long id) {

        authorService.deleteById(id);
    }

    private AuthorDto toDto(Author author) {
        return new AuthorDto(author.getId(), author.getName(), author.getBirthdate());
    }

    private BookDto toBookDto(Book book) {
        return new BookDto(book.getId(), book.getTitle(), book.getYear(), book.getAuthor().getId(), book.getAvailable());
    }
}

