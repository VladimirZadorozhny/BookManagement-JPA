package org.mystudying.bookmanagementjpa.repositories;

import org.mystudying.bookmanagementjpa.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByNameIgnoreCase(String name);

    @Query("SELECT g.name FROM Genre g JOIN g.books b WHERE b.id = :bookId")
    List<String> findNamesByBookId(@Param("bookId") long bookId);

    @Query("SELECT DISTINCT g FROM Genre g LEFT JOIN FETCH g.books b LEFT JOIN FETCH b.author ORDER BY g.name")
    List<Genre> findAllWithBooks();

    boolean existsByNameIgnoreCase(String name);
}
