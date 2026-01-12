-- 1. Authors (using unique name)
INSERT INTO authors (name, birthdate) VALUES ('Logic Author', '1980-01-01');

-- 2. Genres
INSERT INTO genres (name) VALUES ('Logic Genre');

-- 3. Users
INSERT INTO users (name, email) VALUES ('Clean User', 'clean@logic.test');
INSERT INTO users (name, email) VALUES ('Overdue User', 'overdue@logic.test');
INSERT INTO users (name, email) VALUES ('Fine User', 'fine@logic.test');

-- 4. Books
-- Available Book A
INSERT INTO books (title, year, author_id, available) 
VALUES ('Logic Book A', 2020, (SELECT id FROM authors WHERE name = 'Logic Author'), 5);

INSERT INTO book_genres (book_id, genre_id) 
VALUES ((SELECT id FROM books WHERE title = 'Logic Book A'), (SELECT id FROM genres WHERE name = 'Logic Genre'));

-- Available Book B
INSERT INTO books (title, year, author_id, available) 
VALUES ('Logic Book B', 2020, (SELECT id FROM authors WHERE name = 'Logic Author'), 5);

INSERT INTO book_genres (book_id, genre_id) 
VALUES ((SELECT id FROM books WHERE title = 'Logic Book B'), (SELECT id FROM genres WHERE name = 'Logic Genre'));

-- Overdue Book (Held by Overdue User, so available=0)
INSERT INTO books (title, year, author_id, available) 
VALUES ('Overdue Book', 2020, (SELECT id FROM authors WHERE name = 'Logic Author'), 0);

INSERT INTO book_genres (book_id, genre_id) 
VALUES ((SELECT id FROM books WHERE title = 'Overdue Book'), (SELECT id FROM genres WHERE name = 'Logic Genre'));

-- Fined Book (Returned by Fine User, so available=1)
INSERT INTO books (title, year, author_id, available) 
VALUES ('Fined Book', 2020, (SELECT id FROM authors WHERE name = 'Logic Author'), 1);

INSERT INTO book_genres (book_id, genre_id) 
VALUES ((SELECT id FROM books WHERE title = 'Fined Book'), (SELECT id FROM genres WHERE name = 'Logic Genre'));


-- 5. Bookings

-- Scenario: Overdue User has 'Overdue Book'. 
-- Borrowed 20 days ago, Due 6 days ago. Not returned.
INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid) 
VALUES (
    (SELECT id FROM users WHERE email = 'overdue@logic.test'),
    (SELECT id FROM books WHERE title = 'Overdue Book'),
    DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY),
    DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY),
    NULL,
    0.00,
    FALSE
);

-- Scenario: Fine User has 'Fined Book'.
-- Borrowed 30 days ago, Due 16 days ago. Returned 1 day ago.
-- Overdue by 15 days. Fine should be 15.00.
INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid) 
VALUES (
    (SELECT id FROM users WHERE email = 'fine@logic.test'),
    (SELECT id FROM books WHERE title = 'Fined Book'),
    DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY),
    DATE_SUB(CURRENT_DATE, INTERVAL 16 DAY),
    DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY),
    15.00,
    FALSE
);
