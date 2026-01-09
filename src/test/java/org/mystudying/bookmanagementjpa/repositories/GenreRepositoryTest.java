package org.mystudying.bookmanagementjpa.repositories;

import org.junit.jupiter.api.Test;
import org.mystudying.bookmanagementjpa.domain.Genre;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GenreRepositoryTest {
    private final GenreRepository genreRepository;
    public GenreRepositoryTest(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Test
    void findByNameIgnoreCase_shouldReturnGenre() {
        Genre genre = new Genre(null, "FantasyTest");
        genreRepository.save(genre);

        Optional<Genre> found = genreRepository.findByNameIgnoreCase("fantasytest");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("FantasyTest");
    }


}