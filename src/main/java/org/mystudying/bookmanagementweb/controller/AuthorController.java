package org.mystudying.bookmanagementweb.controller;

import jakarta.validation.Valid;
import org.mystudying.bookmanagementweb.domain.Author;
import org.mystudying.bookmanagementweb.domain.Book;
import org.mystudying.bookmanagementweb.dto.AuthorDto;
import org.mystudying.bookmanagementweb.dto.BookDto;
import org.mystudying.bookmanagementweb.dto.CreateAuthorRequestDto;
import org.mystudying.bookmanagementweb.dto.UpdateAuthorRequestDto;
import org.mystudying.bookmanagementweb.exceptions.AuthorNotFoundException;
import org.mystudying.bookmanagementweb.services.AuthorService;
import org.mystudying.bookmanagementweb.services.BookService;
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
        Author author = new Author(1L, authorDto.name(), authorDto.birthdate());
        long newId = authorService.save(author);
        return toDto(new Author(newId, author.getName(), author.getBirthdate()));
    }

    @PutMapping("/{id}")
    public AuthorDto updateAuthor(@PathVariable long id, @Valid @RequestBody UpdateAuthorRequestDto authorDto) {
        authorService.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        Author authorToUpdate = new Author(id, authorDto.name(), authorDto.birthdate());
        authorService.update(authorToUpdate);
        return toDto(authorToUpdate);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable long id) {
        authorService.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        authorService.deleteById(id);
    }

    private AuthorDto toDto(Author author) {
        return new AuthorDto(author.getId(), author.getName(), author.getBirthdate());
    }

    private BookDto toBookDto(Book book) {
        return new BookDto(book.getId(), book.getTitle(), book.getYear(), book.getAuthorId(), book.getAvailable());
    }
}

