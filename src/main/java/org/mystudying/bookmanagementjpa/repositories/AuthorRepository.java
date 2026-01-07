package org.mystudying.bookmanagementjpa.repositories;

import org.mystudying.bookmanagementjpa.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByName(String name);
}
