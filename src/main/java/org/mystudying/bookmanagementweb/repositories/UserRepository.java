package org.mystudying.bookmanagementweb.repositories;


import org.mystudying.bookmanagementweb.domain.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository implements UserRepositoryInt {
    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<User> findAll() {
        var sql = "SELECT id, name, email FROM users ORDER BY name";
        return jdbcClient.sql(sql).query(User.class).list();
    }

    @Override
    public Optional<User> findById(long id) {
        var sql = "SELECT id, name, email FROM users WHERE id = ?";
        return jdbcClient.sql(sql).param(id).query(User.class).optional();
    }

    @Override
    public Optional<User> findByName(String name) {
        var sql = "SELECT id, name, email FROM users WHERE name = ?";
        return jdbcClient.sql(sql).param(name).query(User.class).optional();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        var sql = "SELECT id, name, email FROM users WHERE email = ?";
        return jdbcClient.sql(sql).param(email).query(User.class).optional();
    }

    @Override
    public long save(User user) {
        var sql = "INSERT INTO users(name, email) VALUES (?, ?)";
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
                .params(user.getName(), user.getEmail())
                .update(keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void update(User user) {
        var sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        jdbcClient.sql(sql)
                .params(user.getName(), user.getEmail(), user.getId())
                .update();
    }

    @Override
    public void deleteById(long id) {
        var sql = "DELETE FROM users WHERE id = ?";
        jdbcClient.sql(sql).param(id).update();
    }
}

