package org.mystudying.bookmanagementjpa.repositories;

import org.mystudying.bookmanagementjpa.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);


}
