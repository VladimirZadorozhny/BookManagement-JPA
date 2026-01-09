package org.mystudying.bookmanagementjpa.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.jdbc.JdbcTestUtils;
import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/insertTestRecords.sql")
public class GenreControllerTest {

    private static final String GENRES_TABLE = "genres";
    private static final String BOOK_GENRES_TABLE = "book_genres";

    private final MockMvc mockMvc;
    private final JdbcClient jdbcClient;

    public GenreControllerTest(MockMvc mockMvc, JdbcClient jdbcClient) {
        this.mockMvc = mockMvc;
        this.jdbcClient = jdbcClient;
    }

    private long idOfTestGenre1() {
        return jdbcClient.sql("select id from genres where name = 'Test Genre 1'")
                .query(Long.class)
                .single();
    }

    @Test
    void getAllGenresReturnsAllGenresSorted() throws Exception {
        var amountGenres = JdbcTestUtils.countRowsInTable(jdbcClient, GENRES_TABLE);
        MvcResult result = mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(amountGenres))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<String> names = JsonPath.parse(jsonResponse).read("$[*].name");

        assertThat(names).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
    }

    @Test
    void getGenreByIdReturnsCorrectGenre() throws Exception {
        long id = idOfTestGenre1();
        mockMvc.perform(get("/api/genres/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Test Genre 1"));
    }

    @Test
    void getGenreByIdReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/genres/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBooksByGenreIdReturnsCorrectBooks() throws Exception {
        long id = idOfTestGenre1();
        int amountBooksOfGenre = JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOK_GENRES_TABLE, "genre_id = " + id);
        mockMvc.perform(get("/api/genres/{id}/books", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(amountBooksOfGenre));
    }

    @Test
    void getBooksByGenreNameReturnsCorrectBooks() throws Exception {
        long id = idOfTestGenre1();
        int amountBooksOfGenre = JdbcTestUtils.countRowsInTableWhere(jdbcClient, BOOK_GENRES_TABLE, "genre_id = " + id);
        mockMvc.perform(get("/api/genres/name/{name}/books", "Test Genre 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(amountBooksOfGenre));
    }

    @Test
    void getAllGenresWithBooksReturnsGroupedData() throws Exception {
        mockMvc.perform(get("/api/genres/with-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].books").isArray());
    }
}