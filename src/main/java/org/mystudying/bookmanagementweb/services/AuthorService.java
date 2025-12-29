package org.mystudying.bookmanagementweb.services;


import org.mystudying.bookmanagementweb.domain.Author;
import org.mystudying.bookmanagementweb.exceptions.AuthorHasBooksException;
import org.mystudying.bookmanagementweb.repositories.AuthorRepository;
import org.mystudying.bookmanagementweb.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository; // Inject BookRepository

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) { // Update constructor
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    public Optional<Author> findById(long id) {
        return authorRepository.findById(id);
    }

    public Optional<Author> findByName(String name) {
        return authorRepository.findByName(name);
    }

    @Transactional
    public long save(Author author) {
        return authorRepository.save(author);
    }

    @Transactional
    public void update(Author author) {
        authorRepository.update(author);
    }

    @Transactional
    public void deleteById(long id) {
        if (bookRepository.existsByAuthorId(id)) {
            throw new AuthorHasBooksException(id);
        }
        authorRepository.deleteById(id);
    }
}

