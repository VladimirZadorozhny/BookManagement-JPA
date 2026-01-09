-- Add genres and book-genre relationship

CREATE TABLE IF NOT EXISTS genres (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS book_genres (
    book_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, genre_id),
    CONSTRAINT fk_book_genres_book
        FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_book_genres_genre
        FOREIGN KEY (genre_id) REFERENCES genres(id)
);

-- Seed genres
INSERT INTO genres (name) VALUES
('Science Fiction'),
('Fantasy'),
('Classic'),
('Mystery'),
('Young Adult');

-- Map books to genres
INSERT INTO book_genres (book_id, genre_id)
SELECT b.id, g.id
FROM books b
JOIN genres g ON
    (b.title = 'Foundation' AND g.name IN ('Science Fiction', 'Classic')) OR
    (b.title LIKE 'Harry Potter%' AND g.name IN ('Fantasy', 'Young Adult')) OR
    (b.title LIKE 'A Game of Thrones%' AND g.name = 'Fantasy') OR
    (b.title LIKE 'A Clash of Kings%' AND g.name = 'Fantasy') OR
    (b.title = 'Childhood''s End' AND g.name = 'Science Fiction') OR
    (b.title = 'Rendezvous with Rama' AND g.name = 'Science Fiction') OR
    (b.title LIKE 'Murder on%' AND g.name = 'Mystery') OR
    (b.title = 'And Then There Were None' AND g.name = 'Mystery');
