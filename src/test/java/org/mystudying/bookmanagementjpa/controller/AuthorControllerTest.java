package org.mystudying.bookmanagementjpa.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.jdbc.JdbcTestUtils;
import com.jayway.jsonpath.JsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/insertTestRecords.sql")
public class AuthorControllerTest {

    private static final String AUTHORS_TABLE = "authors";
    private static final String BOOKS_TABLE = "books"; // Added for cleanup

    private final MockMvc mockMvc;
    private final JdbcClient jdbcClient;
    private final EntityManager entityManager;

    public AuthorControllerTest(MockMvc mockMvc, JdbcClient jdbcClient, EntityManager entityManager) {
        this.mockMvc = mockMvc;
        this.jdbcClient = jdbcClient;
        this.entityManager = entityManager;
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

    @Test
    void getAllAuthorsReturnsAllAuthors() throws Exception {
        var amountAuthors = JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORS_TABLE);
        MvcResult result = mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(amountAuthors))
                .andReturn();

        // Extracting and asserting on sorting and content
        String jsonResponse = result.getResponse().getContentAsString();
        List<String> names = JsonPath.parse(jsonResponse).read("$[*].name");

        assertThat(names)
                .isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER) // Check sorting
                .contains("Test Author 1", "Test Author 2", "Author For Deletion"); // Check specific content
    }

    @Test
    void getAuthorByIdReturnsCorrectAuthor() throws Exception {
        long id = idOfTestAuthor1();
        mockMvc.perform(get("/api/authors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Test Author 1"))
                .andExpect(jsonPath("$.birthdate").value(LocalDate.of(1901, 1, 1).toString()));
    }

    @Test
    void getAuthorByIdReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/authors/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Author not found. Id: " + Long.MAX_VALUE));
    }

    @Test
    void createAuthorReturnsCreatedAuthor() throws Exception {
        long initialRowCount = JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORS_TABLE);
        String newAuthorJson = readJsonFile("correctAuthor.json");

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAuthorJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New Author From Test"))
                .andExpect(jsonPath("$.birthdate").value("1980-01-01"));

        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, AUTHORS_TABLE, "name = 'New Author From Test'")).isEqualTo(1);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORS_TABLE)).isEqualTo(initialRowCount + 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"AuthorWithEmptyName.json", "AuthorWithoutName.json", "AuthorWithoutBirthdate.json", "AuthorWithFutureBirthdate.json"})
    void createAuthorReturnsBadRequestForInvalidData(String fileName) throws Exception {
        String invalidAuthorJson = readJsonFile(fileName);
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidAuthorJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAuthorReturnsUpdatedAuthor() throws Exception {
        long id = idOfTestAuthor1();
        String updatedAuthorJson = readJsonFile("updatedAuthor.json");

        mockMvc.perform(put("/api/authors/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedAuthorJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Updated Author Name"))
                .andExpect(jsonPath("$.birthdate").value("1970-05-10"));

        mockMvc.perform(get("/api/authors/name/{name}", "Updated Author Name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

//        extra test to see the changes in DB
        entityManager.flush();
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, AUTHORS_TABLE, "id = " + id + " AND name = 'Updated Author Name'")).isEqualTo(1);
    }

    @Test
    void updateAuthorReturnsNotFoundForUnknownId() throws Exception {
        String updatedAuthorJson = readJsonFile("updatedAuthor.json");
        mockMvc.perform(put("/api/authors/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedAuthorJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Author not found. Id: " + Long.MAX_VALUE));
    }

    @Test
    void deleteAuthorReturnsNoContentIfAuthorHasNoBooks() throws Exception {
        long authorIdToDelete = idOfAuthorForDeletion();

        // Explicitly delete books by this author to avoid foreign key constraint issues only in this test for correct checking of deletion method
        // The Book For Deletion is linked to Author For Deletion
        jdbcClient.sql("DELETE FROM " + BOOKS_TABLE + " WHERE author_id = ?")
                .param(authorIdToDelete)
                .update();

        long initialRowCount = JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORS_TABLE);

        mockMvc.perform(delete("/api/authors/{id}", authorIdToDelete))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/authors/{id}", authorIdToDelete))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/authors"))
                .andExpect(jsonPath("$.length()").value(initialRowCount - 1));


//        extra test to see changes in DB
        entityManager.flush();
        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORS_TABLE)).isEqualTo(initialRowCount - 1);
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, AUTHORS_TABLE, "id = " + authorIdToDelete)).isEqualTo(0);
    }


    @Test
    void deleteAuthorReturnsConflictIfAuthorHasBooks() throws Exception {
        long authorIdToDelete = idOfAuthorForDeletion();

        long initialRowCount = JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORS_TABLE);

        mockMvc.perform(delete("/api/authors/{id}", authorIdToDelete))
                .andExpect(status().isConflict());

        assertThat(JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORS_TABLE)).isEqualTo(initialRowCount);
        assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcClient, AUTHORS_TABLE, "id = " + authorIdToDelete)).isOne();
    }

    @Test
    void deleteAuthorReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(delete("/api/authors/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Author not found. Id: " + Long.MAX_VALUE));
    }

    // Helper method to read JSON from file
    private String readJsonFile(String filename) throws IOException {
        return new ClassPathResource(filename).getContentAsString(StandardCharsets.UTF_8);
    }





}