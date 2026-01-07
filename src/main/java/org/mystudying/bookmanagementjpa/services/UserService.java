package org.mystudying.bookmanagementjpa.services;

import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.domain.User;
import org.mystudying.bookmanagementjpa.dto.CreateUserRequestDto;
import org.mystudying.bookmanagementjpa.dto.UpdateUserRequestDto;
import org.mystudying.bookmanagementjpa.exceptions.*;
import org.mystudying.bookmanagementjpa.repositories.BookRepository;
import org.mystudying.bookmanagementjpa.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public UserService(UserRepository userRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll(Sort.by("name"));
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

//    don't need it anymore since ManyToMany relation between User and Book
    public List<Book> findBooksByUserId(long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return bookRepository.findBooksByUserId(userId);
    }

    @Transactional
    public User save(CreateUserRequestDto createUserRequestDto) {
        try {
            return userRepository.save(new User(null, createUserRequestDto.name(), createUserRequestDto.email()));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(createUserRequestDto.email());
        }
    }

    @Transactional
    public User update(long id, UpdateUserRequestDto updateUserRequestDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        try {
            user.setName(updateUserRequestDto.name());
            user.setEmail(updateUserRequestDto.email());
            userRepository.saveAndFlush(user);
            return user;
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
    }

    @Transactional
    public void deleteById(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (!user.getBooks().isEmpty()) {
            throw new UserHasBookingsException(id);
        }
        userRepository.delete(user);
    }

    @Transactional
    public void rentBook(long userId, long bookId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Book book = bookRepository.findAndLockById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        book.rentBook();
        user.addBook(book);
        book.addUser(user);
    }

    @Transactional
    public void returnBook(long userId, long bookId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Book book = bookRepository.findAndLockById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        user.removeBook(book);
        book.removeUser(user);
        book.returnBook();
    }
}