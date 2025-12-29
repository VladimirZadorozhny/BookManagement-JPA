package org.mystudying.bookmanagementweb.controller;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/insertTestRecords.sql")
public class BookControllerTest {

    private static final String BOOKS_TABLE = "books";
    private static final String AUTHORS_TABLE = "authors";

    private final MockMvc mockMvc;
    private final JdbcClient jdbcClient;

    public BookControllerTest(MockMvc mockMvc, JdbcClient jdbcClient) {
        this.mockMvc = mockMvc;
        this.jdbcClient = jdbcClient;
    }

    private long idOfTestBook1() {
        return jdbcClient.sql("select id from books where title = 'Test Book 1'")
                .query(Long.class)
                .single();
    }

    private long idOfTestAuthor1() {
        return jdbcClient.sql("select id from authors where name = 'Test Author 1'")
                .query(Long.class)
                .single();
    }

    private long idOfAuthorForDeletion() {
        return jdbcClient.sql("select id from authors where name = 'Author For Deletion'")
                .query(Long.class)
                .single();
    }

    private long idOfBookForDeletion() {
        return jdbcClient.sql("SELECT id FROM books WHERE title = 'Book For Deletion'")
                .query(Long.class)
                .single();
    }

    private long idOfRentableBook() {
        return jdbcClient.sql("SELECT id FROM books WHERE title = 'Rentable Book'")
                .query(Long.class)
                .single();
    }

    @Test
    void getAllBooksReturnsAllBooks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(JdbcTestUtils.countRowsInTable(jdbcClient, BOOKS_TABLE)))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> titles = JsonPath.parse(jsonResponse).read("$[*].title");

        assertThat(titles)
                .isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER) // Books are sorted by title in the repository
                .contains("Test Book 1", "Test Book 2", "Book For Deletion", "Rentable Book"); // Check specific content
    }

    @Test
    void getAllBooksReturnsAvailableBooks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books").queryParam("available", "true"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> titles = JsonPath.parse(jsonResponse).read("$[*].title");

        // "Test Book 1" has 5 available, "Test Book 2" has 0 available, "Book For Deletion" has 1, "Rentable Book" has 1
        assertThat(titles).contains("Test Book 1", "Book For Deletion", "Rentable Book");
        assertThat(titles).doesNotContain("Test Book 2");

        List<Integer> available = JsonPath.parse(jsonResponse).read("$[*].available");
        assertThat(available)
                .allSatisfy(amount -> assertThat(amount).isPositive());
    }

    @Test
    void getAllBooksReturnsUnavailableBooks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books").queryParam("available", "false"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> titles = JsonPath.parse(jsonResponse).read("$[*].title");

        assertThat(titles).contains("Test Book 2");
        assertThat(titles).doesNotContain("Test Book 1", "Book For Deletion", "Rentable Book");

        List<Integer> available = JsonPath.parse(jsonResponse).read("$[*].available");
        assertThat(available)
                .allSatisfy(amount -> assertThat(amount).isZero());
    }

    @Test
    void getAllBooksReturnsBooksByYear() throws Exception {
        mockMvc.perform(get("/api/books").queryParam("year", "2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(JdbcTestUtils.countRowsInTableWhere(
                        jdbcClient, BOOKS_TABLE, "year = 2001"
                )));
    }

    @Test
    void getAllBooksReturnsBooksByAuthorName() throws Exception {
        String authorName = "Test Author 1";
        long authorId = idOfTestAuthor1();
        
        MvcResult result = mockMvc.perform(get("/api/books").queryParam("authorName", authorName))
                .andExpect(status().isOk())
                .andReturn();

        long expectedDbCount = jdbcClient.sql("""
                                              SELECT count(b.id)
                                              FROM books b
                                              JOIN authors a ON b.author_id = a.id
                                              WHERE a.name = ?
                                              """)
                .param(authorName)
                .query(Long.class)
                .single();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> titles = JsonPath.parse(jsonResponse).read("$[*].title");

        assertThat(titles).hasSize((int)expectedDbCount);
        assertThat(titles).contains("Test Book 1", "Rentable Book");
    }

    @Test
    void getBookByIdReturnsCorrectBook() throws Exception {
        long id = idOfTestBook1();
        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Test Book 1"));
    }

    @Test
    void getBookByIdReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/books/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found. Id: " + Long.MAX_VALUE));
    }

    @Test
    void getBookByTitleReturnsCorrectBook() throws Exception {
        mockMvc.perform(get("/api/books/title/{title}", "Test Book 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book 1"));
    }

    @Test
    void getBookByTitleReturnsNotFoundForUnknownTitle() throws Exception {
        mockMvc.perform(get("/api/books/title/{title}", "Non Existent Title"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found. Title: Non Existent Title"));
    }

    @Test
    void createBookReturnsCreatedBook() throws Exception {
        long initialRowCount = JdbcTestUtils.countRowsInTable(jdbcClient, BOOKS_TABLE);
        String newBookJson = readJsonFile("correctBook.json");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newBookJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("New Book From Test"));

        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKS_TABLE, "title = 'New Book From Test'")).isEqualTo(1);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, BOOKS_TABLE)).isEqualTo(initialRowCount + 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "BookWithEmptyTitle.json",
        "BookWithoutTitle.json",
        "BookWithFutureYear.json",
        "BookWithoutYear.json",
        "BookWithZeroAuthorId.json",
        "BookWithoutAuthorId.json",
        "BookWithNegativeAvailable.json",
        "BookWithoutAvailable.json"
    })
    void createBookReturnsBadRequestForInvalidData(String fileName) throws Exception {
        String invalidBookJson = readJsonFile(fileName);
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBookJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookReturnsUpdatedBook() throws Exception {
        long id = idOfTestBook1();
        String updatedBookJson = readJsonFile("updatedBook.json");

        mockMvc.perform(put("/api/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedBookJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Updated Book Title"))
                .andExpect(jsonPath("$.year").value(2010));
        
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKS_TABLE, "id = " + id + " AND title = 'Updated Book Title'")).isEqualTo(1);
    }

    @Test
    void updateBookReturnsNotFoundForUnknownId() throws Exception {
        String updatedBookJson = readJsonFile("updatedBook.json");
        mockMvc.perform(put("/api/books/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedBookJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found. Id: " + Long.MAX_VALUE));
    }

    @Test
    void deleteBookReturnsNoContent() throws Exception {
        long id = idOfBookForDeletion();
        long initialRowCount = JdbcTestUtils.countRowsInTable(jdbcClient, BOOKS_TABLE);
        
        mockMvc.perform(delete("/api/books/{id}", id))
                .andExpect(status().isNoContent());
        
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, BOOKS_TABLE)).isEqualTo(initialRowCount - 1);
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOKS_TABLE, "id = " + id)).isEqualTo(0);
    }

    @Test
    void deleteBookReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found. Id: " + Long.MAX_VALUE));
    }

    // Helper method to read JSON from file
    private String readJsonFile(String filename) throws IOException {
        return new ClassPathResource(filename).getContentAsString(StandardCharsets.UTF_8);
    }
}

