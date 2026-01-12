package org.mystudying.bookmanagementjpa.controller;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/insertTestRecords.sql")

public class UserControllerTest {

    private static final String USERS_TABLE = "users";
    private static final String BOOKS_TABLE = "books";
    private static final String BOOKINGS_TABLE = "bookings";
    private static final String AUTHORS_TABLE = "authors"; // Added for final cleanup (because roll back is off in specific test)


    private final MockMvc mockMvc;
    private final JdbcClient jdbcClient;
    private final TransactionTemplate txTemplate;
    private final EntityManager entityManager;

    public UserControllerTest(MockMvc mockMvc, JdbcClient jdbcClient, PlatformTransactionManager txManager, EntityManager entityManager) {
        this.mockMvc = mockMvc;
        this.jdbcClient = jdbcClient;
        this.txTemplate = new TransactionTemplate(txManager);
        this.entityManager = entityManager;
    }

    private long idOfTestUser1() {
        return jdbcClient.sql("select id from users where email = 'test1@example.com'")
                .query(Long.class)
                .single();
    }

    private long idOfTestUser2() {
        return jdbcClient.sql("select id from users where email = 'test2@example.com'")
                .query(Long.class)
                .single();
    }

    private long idOfUserForDeletion() {
        return jdbcClient.sql("select id from users where email = 'delete@example.com'")
                .query(Long.class)
                .single();
    }

    private long idOfRentUser() {
        return jdbcClient.sql("select id from users where email = 'rent@example.com'")
                .query(Long.class)
                .single();
    }

    private long idOfTestBook1() {
        return jdbcClient.sql("select id from books where title = 'Test Book 1'")
                .query(Long.class)
                .single();
    }

    private long idOfRentableBook() {
        return jdbcClient.sql("SELECT id FROM books WHERE title = 'Rentable Book'")
                .query(Long.class)
                .single();
    }

    @Test
    void getAllUsersReturnsAllUsers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE)))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> names = JsonPath.parse(jsonResponse).read("$[*].name");

        assertThat(names)
                .isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER)
                .contains("Test User 1", "Test User 2", "User For Deletion", "Rent User");
    }

    @Test
    void getUserByIdReturnsCorrectUser() throws Exception {
        long id = idOfTestUser1();
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Test User 1"));
    }

    @Test
    void getUserByIdReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/users/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found. Id: " + Long.MAX_VALUE));
    }

    @Test
    void searchUserByNameReturnsCorrectUser() throws Exception {
        mockMvc.perform(get("/api/users/search").queryParam("by", "Test User 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User 1"))
                .andExpect(jsonPath("$.id").value(idOfTestUser1()));
    }

    @Test
    void searchUserByEmailReturnsCorrectUser() throws Exception {
        mockMvc.perform(get("/api/users/search").queryParam("by", "test2@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test2@example.com"))
                .andExpect(jsonPath("$.id").value(idOfTestUser2()));
    }

    @Test
    void searchUserReturnsNotFoundForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/users/search").queryParam("by", "unknown@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found. Name or email: unknown@example.com"));
    }

    @Test
    void getBooksByUserReturnsCorrectBooks() throws Exception {
        long user1Id = idOfTestUser1(); // Test User 1 has Test Book 1

        long expectedBooksCount = jdbcClient.sql("SELECT count(book_id) " +
                        "FROM bookings " +
                        "WHERE user_id = ?")
                .param(user1Id)
                .query(Long.class)
                .single();

        MvcResult result = mockMvc.perform(get("/api/users/{id}/books", user1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value((int) expectedBooksCount))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> titles = JsonPath.parse(jsonResponse).read("$[*].title");

        assertThat(titles)
                .isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER)
                .contains("Test Book 1");
    }

    @Test
    void getBooksByUserReturnsNotFoundForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}/books", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found. Id: " + Long.MAX_VALUE));
    }

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        long initialRowCount = JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE);
        String newUserJson = readJsonFile("correctUser.json");
        long lastId = jdbcClient.sql("select max(id) from users")
                .query(Long.class)
                .single();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(lastId + 1))
                .andExpect(jsonPath("$.name").value("New User From Test"))
                .andExpect(jsonPath("$.email").value("new.test@example.com"));

//        we already can check DB without flushing due the forced flush by creation and strategy = GenerationType.IDENTITY
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, USERS_TABLE, "email = 'new.test@example.com'")).isEqualTo(1);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE)).isEqualTo(initialRowCount + 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UserWithEmptyName.json",
            "UserWithoutName.json",
            "UserWithEmptyEmail.json",
            "UserWithoutEmail.json",
            "UserWithInvalidEmail.json"
    })
    void createUserReturnsBadRequestForInvalidData(String fileName) throws Exception {
        String invalidUserJson = readJsonFile(fileName);
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserReturnsConflictForDuplicateEmail() throws Exception {
        String duplicateUserJson = """
            {
                "name": "Existing User",
                "email": "test1@example.com"
            }
            """;
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateUserJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Email already exists: test1@example.com")));
    }

    @Test
    void updateUserReturnsUpdatedUser() throws Exception {
        long id = idOfTestUser1();
        String updatedUserJson = readJsonFile("updatedUser.json");

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Updated User Name"))
                .andExpect(jsonPath("$.email").value("updated.user@example.com"));

//        we already can check DB without flushing due "saveAndFlush" in Service class
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, USERS_TABLE, "id = " + id + " AND email = 'updated.user@example.com'")).isEqualTo(1);
    }

    @Test
    void updateUserReturnsNotFoundForUnknownId() throws Exception {
        String updatedUserJson = readJsonFile("updatedUser.json");
        mockMvc.perform(put("/api/users/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedUserJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found. Id: " + Long.MAX_VALUE));
    }

    @Test
    void updateUserReturnsConflictForDuplicateEmail() throws Exception {
        long id = idOfTestUser1();
        // Try to update user#1's email to user#2's email
        String duplicateEmailJson = """
            {
                "name": "Test User 1",
                "email": "test2@example.com"
            }
            """;
        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateEmailJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Email already exists: test2@example.com")));

//        we already can check DB without flushing due "saveAndFlush" in Service class
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, USERS_TABLE, "email = 'test1@example.com' and id = " + id)).isEqualTo(1);

//        force to detach all Entities, and also the one that keep old incorrect data from update attempt and to re-read from DB
        entityManager.clear();
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test1@example.com"));
    }


    @Test
    void deleteUserReturnsNoContent() throws Exception {
        long id = idOfUserForDeletion();
        long initialRowCount = JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(initialRowCount - 1));

//        extra test to see the changes in DB
        entityManager.flush();
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE)).isEqualTo(initialRowCount - 1);
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, USERS_TABLE, "id = " + id)).isEqualTo(0);
    }

    @Test
    void deleteUserReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found. Id: " + Long.MAX_VALUE));
    }

    @Test
    void rentBookReturnsNoContent() throws Exception {
        long rentUserId = idOfRentUser();
        long rentableBookId = idOfRentableBook();
        long initialBookings = JdbcTestUtils.countRowsInTable(jdbcClient, BOOKINGS_TABLE);
        long initialBooksByUser = JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE, "user_id = " + rentUserId);
        int initialAvailable = jdbcClient.sql("SELECT available FROM books WHERE id = ?").param(rentableBookId).query(Integer.class).single();

        String rentRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(rentableBookId));

        mockMvc.perform(post("/api/users/{userId}/rent", rentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rentRequestJson))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}/books", rentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(initialBooksByUser + 1))
                .andExpect(jsonPath("$[*]").value(hasSize((int)initialBooksByUser + 1)))
                        .andExpect(jsonPath("$[*].id").value(hasItem((int)rentableBookId)));

        mockMvc.perform(get("/api/books/{id}", rentableBookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(initialAvailable - 1));

//         extra test to see the changes in DB
        entityManager.flush();
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, BOOKINGS_TABLE)).isEqualTo(initialBookings + 1);
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE,
                "user_id = " + rentUserId + " AND book_id = " + rentableBookId)).isEqualTo(1);
        // Verify book available count decreased in DB
        assertThat(jdbcClient.sql("SELECT available FROM books WHERE id = ?").param(rentableBookId).query(Integer.class).single()).isEqualTo(initialAvailable - 1);
    }

    @Test
    void rentBookReturnsNotFoundForUnknownUser() throws Exception {
        long rentableBookId = idOfRentableBook();
        String rentRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(rentableBookId));

        mockMvc.perform(post("/api/users/{userId}/rent", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rentRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found. Id: " + Long.MAX_VALUE)));
    }

    @Test
    void rentBookReturnsNotFoundForUnknownBook() throws Exception {
        long rentUserId = idOfRentUser();
        String rentRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(Long.MAX_VALUE));

        mockMvc.perform(post("/api/users/{userId}/rent", rentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rentRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Book not found. Id: " + Long.MAX_VALUE)));
    }

    @Test
    void rentBookReturnsConflictForAlreadyBorrowedBook() throws Exception {
        long user1Id = idOfTestUser1();
        long testBook1Id = idOfTestBook1(); // Test Book 1 is already borrowed by Test User 1
        String rentRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(testBook1Id));

        mockMvc.perform(post("/api/users/{userId}/rent", user1Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rentRequestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Book is already borrowed by this user.")));
    }

    @Test
    void rentBookReturnsConflictForUnavailableBook() throws Exception {
        long rentUserId = idOfRentUser();
        long unavailableBookId = jdbcClient.sql("SELECT id FROM books WHERE title = 'Test Book 2'").query(Long.class).single();
        String rentRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(unavailableBookId));

        mockMvc.perform(post("/api/users/{userId}/rent", rentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rentRequestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Book with id '" + unavailableBookId + "' is not available.")));
    }


    @Test
    void returnBookReturnsNoContent() throws Exception {
        // First, make sure that a book is rented by Rent User
        long rentUserId = idOfRentUser();
        long rentableBookId = idOfRentableBook();
        String rentRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(rentableBookId));
        mockMvc.perform(post("/api/users/{userId}/rent", rentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rentRequestJson))
                .andExpect(status().isNoContent());

        MvcResult resultRentedBook = mockMvc.perform(get("/api/books/{id}", rentableBookId))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = resultRentedBook.getResponse().getContentAsString();
        int initialAvailable = JsonPath.read(jsonResponse, "$.available");

        MvcResult resultBooksByUser = mockMvc.perform(get("/api/users/{id}/books", rentUserId))
                .andExpect(status().isOk())
                .andReturn();
        String jsonBooksByUser = resultBooksByUser.getResponse().getContentAsString();
        int booksByUser = JsonPath.read(jsonBooksByUser, "$.length()");

//    If our POST request have passed - the rent method is succeeded and booking was created.
//    We expect the row count to remain the same after return (historical record kept).
        int initialBookings = JdbcTestUtils.countRowsInTable(jdbcClient, BOOKINGS_TABLE);

        String returnRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(rentableBookId));

        mockMvc.perform(post("/api/users/{userId}/return", rentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnRequestJson))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}", rentableBookId))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$.available").value(initialAvailable + 1));

        mockMvc.perform(get("/api/users/{id}/books", rentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(booksByUser - 1))
                .andExpect(jsonPath("$[?(@.id == " + rentableBookId + ")]").isEmpty());

//      extra test to see the changes in DB
        entityManager.flush();
        // Booking should still exist (history), but should be marked returned
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, BOOKINGS_TABLE)).isEqualTo(initialBookings); 
        
        // Check that active booking is gone (count of active bookings for this pair is 0)
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE, "returned_at IS NULL AND user_id = "
                        + rentUserId + " AND book_id = " + rentableBookId))
                .isEqualTo(0);

        // Check that returned booking exists
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE, "returned_at IS NOT NULL AND user_id = "
                + rentUserId + " AND book_id = " + rentableBookId))
                .isEqualTo(1);

        assertThat(jdbcClient.sql("SELECT available FROM books WHERE id = ?").param(rentableBookId).query(Integer.class).single()).isEqualTo(initialAvailable + 1);
    }

    @Test
    void returnBookReturnsNotFoundForUnknownUser() throws Exception {
        long rentableBookId = idOfRentableBook();
        String returnRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(rentableBookId));

        mockMvc.perform(post("/api/users/{userId}/return", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found. Id: " + Long.MAX_VALUE)));
    }

    @Test
    void returnBookReturnsNotFoundForUnknownBook() throws Exception {
        long rentUserId = idOfRentUser();
        String returnRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(Long.MAX_VALUE));

        mockMvc.perform(post("/api/users/{userId}/return", rentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Book not found. Id: " + Long.MAX_VALUE)));
    }

    @Test
    void returnBookReturnsConflictForNotBorrowedBook() throws Exception {
        long rentUserId = idOfRentUser();
        long testBook2Id = jdbcClient.sql("SELECT id FROM books WHERE title = 'Test Book 2'").query(Long.class).single(); // Test Book 2 is not borrowed by Rent User
        String returnRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(testBook2Id));

        mockMvc.perform(post("/api/users/{userId}/return", rentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnRequestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("User does not have this book.")));
    }

    // Helper method to read JSON from file
    private String readJsonFile(String filename) throws IOException {
        return new ClassPathResource(filename).getContentAsString(StandardCharsets.UTF_8);
    }

    // Helper method for manual cleanup in concurrency tests
    // This cleanup is targeted to undo the specific changes made by the concurrency test,
    // and also to clean up any test records inserted by @Sql.
    private void dbCleanup(long user1Id, long user2Id, long bookId) {
        // Order: bookings -> book_genres -> books -> authors -> users -> genres

        // 1. Delete all bookings involving these users or this book
        jdbcClient.sql("DELETE FROM " + BOOKINGS_TABLE + " WHERE user_id = ? OR user_id = ? OR book_id = ?")
                .param(user1Id).param(user2Id).param(bookId).update();

        // 2. Delete all records from insertTestRecords.sql
        // Clear bookings first
        jdbcClient.sql("DELETE FROM " + BOOKINGS_TABLE + " WHERE user_id IN (SELECT id FROM " + USERS_TABLE + " WHERE email IN ('delete@example.com', 'rent@example.com', 'test1@example.com', 'test2@example.com'))").update();
        
        // Clear book_genres
        jdbcClient.sql("DELETE FROM book_genres WHERE book_id IN (SELECT id FROM " + BOOKS_TABLE + " WHERE title IN ('Book For Deletion', 'Rentable Book', 'Test Book 1', 'Test Book 2'))").update();

        // Clear books
        jdbcClient.sql("DELETE FROM " + BOOKS_TABLE + " WHERE title IN ('Book For Deletion', 'Rentable Book', 'Test Book 1', 'Test Book 2')").update();

        // Clear authors, users, genres
        jdbcClient.sql("DELETE FROM " + AUTHORS_TABLE + " WHERE name IN ('Author For Deletion', 'Test Author 1', 'Test Author 2')").update();
        jdbcClient.sql("DELETE FROM " + USERS_TABLE + " WHERE email IN ('delete@example.com', 'rent@example.com', 'test1@example.com', 'test2@example.com')").update();
        jdbcClient.sql("DELETE FROM genres WHERE name IN ('Test Genre 1', 'Test Genre 2', 'Test Genre 3')").update();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // Important for concurrency tests due @Transactional also in Service class that is used here in test
    void rentBook_concurrentAccess_oneSucceedsOneFails() throws Exception {
        long user1Id = idOfRentUser();
        long user2Id = idOfTestUser2();
        long bookId = idOfRentableBook(); // This book has 1 available initially

        // Setup: ensure the book is available and not booked by anyone.

        txTemplate.execute(status -> {
            jdbcClient.sql("DELETE FROM " + BOOKINGS_TABLE + " WHERE book_id = ?").param(bookId).update();
            jdbcClient.sql("UPDATE " + BOOKS_TABLE + " SET available = 1 WHERE id = ?").param(bookId).update();
            return null;
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Define two tasks, one for each user that are trying to rent the same book.
        Callable<Integer> task1 = () -> {
            String rentRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(bookId));
            return mockMvc.perform(post("/api/users/{userId}/rent", user1Id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rentRequestJson))
                    .andReturn().getResponse().getStatus();
        };

        Callable<Integer> task2 = () -> {
            String rentRequestJson = readJsonFile("rentOrReturnBookRequest.json").replace("1", String.valueOf(bookId));
            return mockMvc.perform(post("/api/users/{userId}/rent", user2Id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rentRequestJson))
                    .andReturn().getResponse().getStatus();
        };

        List<Callable<Integer>> tasks = new ArrayList<>(List.of(task1, task2));

        try {
            // Invoke both tasks concurrently and collect their status codes
            List<Integer> statusCodes = executor.invokeAll(tasks)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            // Assert that one request succeeded (204) and the other - failed (409), but without guaranty which task did the job first; we check the set content but not the codes' order
            assertThat(statusCodes).containsExactlyInAnyOrder(204, 409);

            // Verify final state in the database
            assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKINGS_TABLE, "book_id = " + bookId)).isEqualTo(1);
            assertThat(jdbcClient.sql("SELECT available FROM " + BOOKS_TABLE + " WHERE id = ?").param(bookId).query(Integer.class).single()).isEqualTo(0);

        } finally {
            executor.shutdown();
            txTemplate.execute(status -> {
                dbCleanup(user1Id, user2Id, bookId);
                return null;
            });
        }
    }



}