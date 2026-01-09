package org.mystudying.bookmanagementjpa.services;

import org.mystudying.bookmanagementjpa.dto.BookDto;
import org.mystudying.bookmanagementjpa.dto.GenreDto;
import org.mystudying.bookmanagementjpa.dto.GenreWithBooksDto;
import org.mystudying.bookmanagementjpa.exceptions.GenreNotFoundException;
import org.mystudying.bookmanagementjpa.repositories.BookRepository;
import org.mystudying.bookmanagementjpa.repositories.GenreRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GenreService {

    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    public GenreService(GenreRepository genreRepository, BookRepository bookRepository) {
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
    }

    public List<GenreDto> findAll() {
        return genreRepository.findAll(Sort.by("name")).stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .toList();
    }

    public Optional<GenreDto> findById(long id) {
        return genreRepository.findById(id)
                .map(genre -> new GenreDto(genre.getId(), genre.getName()));
    }

    public List<BookDto> findBooksByGenre(String genreName) {
        if (!genreRepository.existsByNameIgnoreCase(genreName)) {
            throw new GenreNotFoundException(genreName);
        }
        return bookRepository.findByGenres_NameIgnoreCase(genreName)
                .stream()
                .map(book -> new BookDto(
                        book.getId(),
                        book.getTitle(),
                        book.getYear(),
                        book.getAuthor().getId(),
                        book.getAvailable()
                        ))
                .toList();
    }

    public List<GenreWithBooksDto> findAllWithBooks() {
        return genreRepository.findAllWithBooks().stream()
                .map(genre -> new GenreWithBooksDto(
                        genre.getId(),
                        genre.getName(),
                        genre.getBooks().stream()
                                .map(book -> new BookDto(
                                        book.getId(),
                                        book.getTitle(),
                                        book.getYear(),
                                        book.getAuthor().getId(),
                                        book.getAvailable()
                                ))
                                .toList()
                ))
                .toList();
    }

    public List<BookDto> findBooksByGenreId(long id) {
        genreRepository.findById(id)
                .orElseThrow(() -> new GenreNotFoundException(id));
        return bookRepository.findByGenres_Id(id).stream()
                .map(book -> new BookDto(book.getId(), book.getTitle(), book.getYear(), book.getAuthor().getId(), book.getAvailable()))
                .collect(Collectors.toList());
    }
}
