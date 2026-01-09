package org.mystudying.bookmanagementjpa.repositories;

import jakarta.persistence.LockModeType;
import org.mystudying.bookmanagementjpa.domain.Author;
import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.dto.BookDetailDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByYear(int year);

    List<Book> findByAuthor_Id(Long authorId);

    List<Book> findByGenres_Id(Long genreId);

    List<Book> findByGenres_NameIgnoreCase(String name);

    @Query("SELECT b FROM Book b JOIN b.author a WHERE a.name = :authorName ORDER BY b.title")
    List<Book> findByAuthorName(@Param("authorName") String authorName);

    @Query("SELECT b FROM Book b WHERE (:available = true AND b.available > 0) OR (:available = false AND b.available = 0) ORDER BY b.title")
    List<Book> findByAvailability(@Param("available") boolean available);

    Optional<Book> findByTitle(String title);

    @Query("SELECT b FROM Book b JOIN b.bookings bk WHERE bk.user.id = :userId AND bk.returnedAt IS NULL ORDER BY b.title")
    List<Book> findBooksByUserId(@Param("userId") long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findAndLockById(@Param("id") long id);

    @Query("SELECT new BookDetailDto(b.id, b.title, b.year, b.available, a.name, a.id) " +
           "FROM Book b JOIN b.author a WHERE b.id = :id")
    Optional<BookDetailDto> findBookDetailsById(@Param("id") long id);


    List<Book> findByTitleContainingOrderByTitle(String title);

    @Query("SELECT b FROM Book b JOIN b.author a WHERE a.name LIKE %:authorName% ORDER BY b.title")
    List<Book> findByAuthorNameContaining(@Param("authorName") String authorName);

    boolean existsByAuthor(Author  author);


}
