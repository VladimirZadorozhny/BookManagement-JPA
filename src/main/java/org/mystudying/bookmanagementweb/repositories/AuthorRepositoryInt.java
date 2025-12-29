package org.mystudying.bookmanagementweb.repositories;

import org.mystudying.bookmanagementweb.domain.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorRepositoryInt {
    List<Author> findAll();
    Optional<Author> findById(long id);
    Optional<Author> findByName(String name);
    long save(Author author);
    void update(Author author);
    void deleteById(long id);
}

