package org.mystudying.bookmanagementjpa.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.mystudying.bookmanagementjpa.exceptions.BookAlreadyBorrowedException;
import org.mystudying.bookmanagementjpa.exceptions.BookNotAvailableException;
import org.mystudying.bookmanagementjpa.exceptions.BookNotBorrowedException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Book title cannot be blank.")
    @Column(nullable = false)
    private String title;

    @Min(value = 1, message = "Book year must be positive.")
    @Column(nullable = false)
    private int year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Min(value = 0, message = "Book available must be not negative.")
    @Column(nullable = false)
    private int available;

    @ManyToMany(mappedBy = "books")
    private Set<User> users = new HashSet<>();

    protected Book() {
        // Required by JPA
    }

    public Book(Long id, String title, int year, Author author, int available) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.author = author;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public Author getAuthor() {
        return author;
    }

    public int getAvailable() {
        return available;
    }

    public Set<User> getUsers() {
        return Collections.unmodifiableSet(users);
    }

    public void addUser(User user) {
        if (!users.add(user)) {
            throw new BookAlreadyBorrowedException();
        }
    }

    public void removeUser(User user) {
        if (!users.remove(user)) {
            throw new BookNotBorrowedException();
        }
    }

    public void rentBook() {
        if (available < 1) {
            throw new BookNotAvailableException(id);
        }
        this.available -= 1;
    }

    public void returnBook() {
        this.available += 1;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + "'" +
                ", year=" + year +
                ", authorId=" + (author != null ? author.getId() : "null") +
                ", available=" + available +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return id != null && id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void setAvailable(int available) {
        this.available = available;
    }
}