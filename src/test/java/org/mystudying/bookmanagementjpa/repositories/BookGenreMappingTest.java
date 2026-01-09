package org.mystudying.bookmanagementjpa.repositories;

import org.junit.jupiter.api.Test;
import org.mystudying.bookmanagementjpa.domain.Author;
import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.domain.Genre;
import org.mystudying.bookmanagementjpa.exceptions.BookNotFoundException;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookGenreMappingTest {

    private final BookRepository bookRepository;


    private final GenreRepository genreRepository;


    private final TestEntityManager em;


    public BookGenreMappingTest(BookRepository bookRepository, GenreRepository genreRepository, TestEntityManager em) {
        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
        this.em = em;
    }

    @Test
    void bookCanHaveMultipleGenres() {
        Genre fantasy = genreRepository.save(new Genre(null, "FantasyTest"));
        Genre classic = genreRepository.save(new Genre(null, "ClassicTest"));

        Author author = em.persist(new Author(null, "Test Author", LocalDate.now()));

        Book book = new Book(null, "Test Book", 2000, author, 1);
        book.addGenre(fantasy);
        book.addGenre(classic);

        bookRepository.save(book);
        em.flush();
        em.clear();

        Book found = bookRepository.findById(book.getId()).orElseThrow(() -> new BookNotFoundException(Long.MAX_VALUE));

        assertThat(found.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("FantasyTest", "ClassicTest");
    }

}
