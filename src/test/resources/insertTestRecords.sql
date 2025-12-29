-- Test Authors
INSERT INTO authors(name, birthdate) VALUES ('Test Author 1', '1901-01-01');
INSERT INTO authors(name, birthdate) VALUES ('Test Author 2', '1902-02-02');
INSERT INTO authors(name, birthdate) VALUES ('Author For Deletion', '1950-01-01');

-- Test Users
INSERT INTO users(name, email) VALUES ('Test User 1', 'test1@example.com');
INSERT INTO users(name, email) VALUES ('Test User 2', 'test2@example.com');
INSERT INTO users(name, email) VALUES ('User For Deletion', 'delete@example.com');
INSERT INTO users(name, email) VALUES ('Rent User', 'rent@example.com');

-- Test Books
INSERT INTO books(title, year, author_id, available) VALUES ('Test Book 1', 2001, (SELECT id FROM authors WHERE name='Test Author 1'), 5);
INSERT INTO books(title, year, author_id, available) VALUES ('Test Book 2', 2002, (SELECT id FROM authors WHERE name='Test Author 2'), 0);
INSERT INTO books(title, year, author_id, available) VALUES ('Book For Deletion', 2023, (SELECT id FROM authors WHERE name='Author For Deletion'), 1);
INSERT INTO books(title, year, author_id, available) VALUES ('Rentable Book', 2023, (SELECT id FROM authors WHERE name='Test Author 1'), 1);


-- Test Bookings
INSERT INTO bookings(user_id, book_id) VALUES ((SELECT id FROM users WHERE email='test1@example.com'), (SELECT id FROM books WHERE title='Test Book 1'));