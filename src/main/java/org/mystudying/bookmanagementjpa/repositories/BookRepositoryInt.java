package org.mystudying.bookmanagementjpa.repositories;


import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.dto.BookDetailDto;

import java.util.List;
import java.util.Optional;

public interface BookRepositoryInt {
    List<Book> findAll();
    List<Book> findByYear(int year);
    List<Book> findByAuthorId(long authorId);
    List<Book> findByAuthorName(String authorName);
    List<Book> findByAvailability(boolean available);
    Optional<Book> findById(long id);
    Optional<Book> findByTitle(String title);
    long save(Book book);
    void update(Book book);
    void deleteById(long id);
    List<Book> findBooksByUserId(long userId);

    Optional<Book> findAndLockById(long id);

    Optional<BookDetailDto> findBookDetailsById(long id);

    List<Book> findByTitleContaining(String title);

    List<Book> findByAuthorNameContaining(String authorName);

    boolean existsByAuthorId(long authorId);

}

