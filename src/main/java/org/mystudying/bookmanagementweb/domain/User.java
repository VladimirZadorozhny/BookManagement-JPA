package org.mystudying.bookmanagementweb.domain;

import java.util.regex.Pattern;

public class User {
    private final long id;
    private final String name;
    private final String email;

    private static final String EMAIL_REGEXP = "[-.\\w]+@([\\w-]+\\.)+[\\w-]+";

    public User(long id, String name, String email) {

        if (id < 1)
            throw new IllegalArgumentException("Id must be positive.");
        if (name.isBlank())
            throw new IllegalArgumentException("Name must not be blank.");
        if (email == null || !Pattern.matches(EMAIL_REGEXP, email))
            throw new IllegalArgumentException("Wrong format of email!");

        this.id = id;
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", email='" + email + "'" +
                '}';
    }
}

