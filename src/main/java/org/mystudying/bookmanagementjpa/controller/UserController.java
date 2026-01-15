package org.mystudying.bookmanagementjpa.controller;

import jakarta.validation.Valid;
import org.mystudying.bookmanagementjpa.domain.Book;
import org.mystudying.bookmanagementjpa.domain.User;
import org.mystudying.bookmanagementjpa.dto.*;
import org.mystudying.bookmanagementjpa.exceptions.UserNotFoundException;
import org.mystudying.bookmanagementjpa.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }



    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        return userService.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @GetMapping("/search")
    public UserDto searchUser(@RequestParam String by) {
        Optional<User> user = userService.findByName(by);
        if (user.isEmpty()) {
            user = userService.findByEmail(by);
        }
        return user.map(this::toDto)
                .orElseThrow(() -> new UserNotFoundException(by));
    }

    /**
     * @deprecated Since introduction of Booking entity.
     * Use BookingRepository instead.
     * Kept temporarily to avoid breaking existing tests.
     */
    @Deprecated
    @GetMapping("/{id}/books")
    public List<BookDto> getBooksByUser(@PathVariable long id) {
        return userService.findActiveBorrowedBooksByUserId(id).stream()
                .map(this::toBookDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody CreateUserRequestDto userDto) {
        return toDto(userService.save(userDto));
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable long id, @Valid @RequestBody UpdateUserRequestDto userDto) {
        return toDto(userService.update(id, userDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long id) {
        userService.deleteById(id);
    }

    @PostMapping("/{userId}/rent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rentBook(@PathVariable long userId, @Valid @RequestBody BookActionRequestDto requestDto) {
        userService.rentBook(userId, requestDto.bookId());
    }

    @PostMapping("/{userId}/return")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnBook(@PathVariable long userId, @Valid @RequestBody BookActionRequestDto requestDto) {
        userService.returnBook(userId, requestDto.bookId());
    }

    @GetMapping("/{id}/bookings")
    public List<BookingResponseDto> getUserBookings(@PathVariable long id) {
        return userService.findBookingsByUserId(id);
    }

    @PostMapping("/{userId}/bookings/{bookingId}/pay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void payFine(@PathVariable long userId, @PathVariable long bookingId) {
        userService.payFine(userId, bookingId);
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
    
    private BookDto toBookDto(Book book) {
        return new BookDto(book.getId(), book.getTitle(), book.getYear(), book.getAvailable());
    }
}

