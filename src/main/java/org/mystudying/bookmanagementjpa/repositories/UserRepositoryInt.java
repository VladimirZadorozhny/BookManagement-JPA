package org.mystudying.bookmanagementjpa.repositories;


import org.mystudying.bookmanagementjpa.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryInt {
    List<User> findAll();
    Optional<User> findById(long id);
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    long save(User user);
    void update(User user);
    void deleteById(long id);
}

