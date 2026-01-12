package org.mystudying.bookmanagementjpa.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.mystudying.bookmanagementjpa.dto.BookingResponseDto;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql({"/insertTestRecords.sql", "/insertUserLogicTestRecords.sql"})
public class UserRentLogicTest {

    private final MockMvc mockMvc;
    private final JdbcClient jdbcClient;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    public UserRentLogicTest(MockMvc mockMvc, JdbcClient jdbcClient, EntityManager entityManager, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.jdbcClient = jdbcClient;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    private long idOfUser(String email) {
        return jdbcClient.sql("SELECT id FROM users WHERE email = ?").param(email).query(Long.class).single();
    }

    private long idOfBook(String title) {
        return jdbcClient.sql("SELECT id FROM books WHERE title = ?").param(title).query(Long.class).single();
    }

    @Test
    void rentBookSuccessForCleanUser() throws Exception {
        long userId = idOfUser("clean@logic.test");
        long bookId = idOfBook("Logic Book A");

        long activeBookings = JdbcTestUtils.countRowsInTableWhere(jdbcClient, "bookings",
                "returned_at IS NULL AND user_id = " + userId + " AND book_id = " +  bookId);

        MvcResult resultBook = mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponseBook = resultBook.getResponse().getContentAsString();
        int initialAvailable = JsonPath.read(jsonResponseBook, "$.available");

        MvcResult resultUserBookings = mockMvc.perform(get("/api/users/{id}/bookings", userId))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponseBooking = resultUserBookings.getResponse().getContentAsString();
        int initialBookings = JsonPath.read(jsonResponseBooking, "$.length()");
        List<Long> booksByUser = JsonPath.parse(jsonResponseBooking).read("$[*].bookId");
        assertThat(booksByUser).doesNotContain(bookId);

        String requestJson = String.format("{\"bookId\": %d}", bookId);

        mockMvc.perform(post("/api/users/{userId}/rent", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(initialAvailable - 1));

        mockMvc.perform(get("/api/users/{id}/bookings", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(initialBookings  + 1))
                .andExpect(jsonPath("$[?(@.bookId == " + bookId + ")]").isNotEmpty())
                .andExpect(jsonPath("$[*].bookId").value(hasItem((int)bookId)));

        // extra test to see the changes in DB
        entityManager.flush();
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, "bookings",
                "returned_at IS NULL AND user_id = " + userId + " AND book_id = " +  bookId))
                .isEqualTo(activeBookings + 1);
    }

    @Test
    void rentBookFailsWhenUserHasOverdueBooks() throws Exception {
        long userId = idOfUser("overdue@logic.test");
        long bookId = idOfBook("Logic Book A");

        String requestJson = String.format("{\"bookId\": %d}", bookId);

        mockMvc.perform(post("/api/users/{userId}/rent", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("User with id " + userId + " has overdue books!")));
    }

    @Test
    void rentBookFailsWhenUserHasUnpaidFines() throws Exception {
        long userId = idOfUser("fine@logic.test");
        long bookId = idOfBook("Logic Book A");

        String requestJson = String.format("{\"bookId\": %d}", bookId);

        mockMvc.perform(post("/api/users/{userId}/rent", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("User with id " + userId + " has unpaid fines!")));
    }

    @Test
    void returnBookCalculatesFineWhenOverdue() throws Exception {

        long userId = idOfUser("overdue@logic.test");
        long bookId = idOfBook("Overdue Book");

        String requestJson = String.format("{\"bookId\": %d}", bookId);

        mockMvc.perform(post("/api/users/{userId}/return", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());

        entityManager.flush();
        
        // Verify fine is calculated (should be 6.00 based on DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY) as due_at)
        BigDecimal fine = jdbcClient.sql("SELECT fine FROM bookings WHERE user_id = ? AND book_id = ?")
                .param(userId).param(bookId).query(BigDecimal.class).single();
        
        assertThat(fine).isGreaterThan(BigDecimal.ZERO);
        assertThat(fine.stripTrailingZeros()).isEqualTo(new BigDecimal("6"));
    }

    @Test
    void rentBookSuccessOnlyAfterReturnOverdueAndPayFines() throws Exception {
        long userId = idOfUser("overdue@logic.test");
        long bookIdOverdue = idOfBook("Overdue Book");
        long bookIdClean = idOfBook("Logic Book A");

        String requestJsonCleanBook = String.format("{\"bookId\": %d}", bookIdClean);

//      try to rent a book with overdue book, failed with conflict due overdue book
        mockMvc.perform(post("/api/users/{userId}/rent", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJsonCleanBook))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("User with id " + userId + " has overdue books!")));

//      return overdue book
        String requestJsonOverdueBook = String.format("{\"bookId\": %d}", bookIdOverdue);
        mockMvc.perform(post("/api/users/{userId}/return", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJsonOverdueBook))
                .andExpect(status().isNoContent());

//        check the fines after returning overdue book
        MvcResult resultUserBookings = mockMvc.perform(get("/api/users/{id}/bookings", userId))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponseBooking = resultUserBookings.getResponse().getContentAsString();
        List<BookingResponseDto> bookingsByUser =
                objectMapper.readValue(
                        jsonResponseBooking,
                        new TypeReference<List<BookingResponseDto>>() {}
                );
//        check that we have unpaid fine after returning overdue book
        assertThat(bookingsByUser)
                .anyMatch(booking -> booking.bookId() == bookIdOverdue && !booking.finePaid());

//        check the amount of unpaid fine, it must be $6
        BigDecimal fine = bookingsByUser.stream()
                .filter(el -> el.bookId() == bookIdOverdue)
                .map(BookingResponseDto::fine)
                .findFirst()
                .orElseThrow();
        assertThat(fine).isEqualByComparingTo("6.00");

//      try to rent a book after returning overdue book but before paying fines, failed with conflict due unpaid fines
        mockMvc.perform(post("/api/users/{userId}/rent", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJsonCleanBook))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("User with id " + userId + " has unpaid fines!")));

//        pay fines of booking (with overdue returned book)
        long bookingId = jdbcClient.sql("SELECT id FROM bookings WHERE user_id = ? AND book_id = ?")
                .param(userId).param(bookIdOverdue).query(Long.class).single();

        mockMvc.perform(post("/api/users/{userId}/bookings/{bookingId}/pay",  userId, bookingId));
//        check that we do not have unpaid fines by this booking (with overdue book)
        resultUserBookings = mockMvc.perform(get("/api/users/{id}/bookings", userId))
                .andExpect(status().isOk())
                .andReturn();
        jsonResponseBooking = resultUserBookings.getResponse().getContentAsString();
        bookingsByUser =
                objectMapper.readValue(
                        jsonResponseBooking,
                        new TypeReference<List<BookingResponseDto>>() {}
                );
        assertThat(bookingsByUser)
                .noneMatch(booking -> booking.id() == bookingId && !booking.finePaid());

        BookingResponseDto bookingDto = bookingsByUser.stream()
                .filter(el -> el.id() == bookingId)
                .findFirst()
                .orElseThrow();
        assertThat(bookingDto.fine()).isEqualByComparingTo("6");
        assertThat(bookingDto.finePaid()).isTrue();

//      after insuring that we returned overdue book and paid the fines, try to rent a new book, must succeed
        mockMvc.perform(post("/api/users/{userId}/rent", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJsonCleanBook))
                .andExpect(status().isNoContent());

    }
}
