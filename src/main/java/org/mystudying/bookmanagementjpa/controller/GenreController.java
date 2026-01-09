package org.mystudying.bookmanagementjpa.controller;

import org.mystudying.bookmanagementjpa.dto.BookDto;
import org.mystudying.bookmanagementjpa.dto.GenreDto;
import org.mystudying.bookmanagementjpa.dto.GenreWithBooksDto;
import org.mystudying.bookmanagementjpa.exceptions.GenreNotFoundException;
import org.mystudying.bookmanagementjpa.services.GenreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;


    public GenreController(GenreService genreService) {
        this.genreService = genreService;

    }

    @GetMapping
    public List<GenreDto> getAllGenres() {
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    public GenreDto getGenreById(@PathVariable long id) {
        return genreService.findById(id)
                .orElseThrow(() -> new GenreNotFoundException(id));
    }

    @GetMapping("/with-books")
    public List<GenreWithBooksDto> getAllGenresWithBooks() {
        return genreService.findAllWithBooks();
    }

    @GetMapping("/{id}/books")
    public List<BookDto> getBooksByGenreId(@PathVariable long id) {
        return genreService.findBooksByGenreId(id);
    }

    @GetMapping("/name/{name}/books")
    public List<BookDto> getBooksByGenre(@PathVariable String name) {
        return genreService.findBooksByGenre(name);
    }
}
