package org.mystudying.bookmanagementjpa.domain;

import java.time.LocalDate;

public class Author {
    private final long id;
    private final String name;
    private final LocalDate birthdate;

    public Author(long id, String name, LocalDate birthdate) {

        if (id < 1)
            throw new IllegalArgumentException("Author ID must be positive.");
        if (name.isBlank())
            throw new IllegalArgumentException("Author name must not be empty.");
        if (birthdate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Author birthdate must not be after now.");

        this.id = id;
        this.name = name;
        this.birthdate = birthdate;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", birthdate=" + birthdate +
                '}';
    }
}

