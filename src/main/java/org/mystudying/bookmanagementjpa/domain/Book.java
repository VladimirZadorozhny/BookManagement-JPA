package org.mystudying.bookmanagementjpa.domain;

import java.time.LocalDate;

public class Book {
    private final long id;
    private final String title;
    private final int year;
    private final long authorId;
    private int available;

    public Book(long id, String title, int year, long authorId, int available) {
       if (id < 1)
           throw new IllegalArgumentException("Book id must be positive.");
       if (title.isBlank())
           throw new IllegalArgumentException("Book title cannot be blank.");
       if (LocalDate.now().getYear() < year || year < 1)
           throw new IllegalArgumentException("Book year must be less or equal to current year and positive.");
       if (authorId < 1)
           throw new IllegalArgumentException("Author id must be positive.");
       if (available < 0)
           throw new IllegalArgumentException("Book available must be not negative.");

        this.id = id;
        this.title = title;
        this.year = year;
        this.authorId = authorId;
        this.available = available;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public long getAuthorId() {
        return authorId;
    }

    public int getAvailable() {
        return available;
    }

    public void rentBook() {
        if (available < 1) {
            throw new IllegalArgumentException("Book is not available.");
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
                ", authorId=" + authorId +
                ", available=" + available +
                '}';
    }
}

