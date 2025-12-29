-- Flyway migration: initial demo data

-- USERS
INSERT INTO users (name, email) VALUES
('Alice Johnson', 'alice@example.com'),
('Bob Smith', 'bob@example.com'),
('Charlie Brown', 'charlie@example.com'),
('Diana Prince', 'diana@example.com'),
('Ethan Clark', 'ethan@example.com'),
('Fiona Adams', 'fiona@example.com'),
('George Miller', 'george@example.com'),
('Hannah White', 'hannah@example.com'),
('Ian Black', 'ian@example.com'),
('Julia Davis', 'julia@example.com');

-- AUTHORS
INSERT INTO authors (name, birthdate) VALUES
('Isaac Asimov', '1920-01-02'),
('J.K. Rowling', '1965-07-31'),
('George R.R. Martin', '1948-09-20'),
('Arthur C. Clarke', '1917-12-16'),
('Agatha Christie', '1890-09-15');

-- BOOKS
INSERT INTO books (title, year, author_id, available) VALUES
('Foundation', 1951, (SELECT id FROM authors WHERE name='Isaac Asimov'), 3),
('Harry Potter and the Sorcerer''s Stone', 1997, (SELECT id FROM authors WHERE name='J.K. Rowling'), 5),
('A Game of Thrones', 1996, (SELECT id FROM authors WHERE name='George R.R. Martin'), 0),
('Childhood''s End', 1953, (SELECT id FROM authors WHERE name='Arthur C. Clarke'), 2),
('Murder on the Orient Express', 1934, (SELECT id FROM authors WHERE name='Agatha Christie'), 4),
('I, Robot', 1950, (SELECT id FROM authors WHERE name='Isaac Asimov'), 1),
('Harry Potter and the Chamber of Secrets', 1998, (SELECT id FROM authors WHERE name='J.K. Rowling'), 2),
('A Clash of Kings', 1998, (SELECT id FROM authors WHERE name='George R.R. Martin'), 0),
('Rendezvous with Rama', 1973, (SELECT id FROM authors WHERE name='Arthur C. Clarke'), 3),
('And Then There Were None', 1939, (SELECT id FROM authors WHERE name='Agatha Christie'), 5);

-- BOOKINGS
INSERT INTO bookings (user_id, book_id)
SELECT u.id, b.id FROM users u JOIN books b ON 1=0; -- no-op to ensure statement form for some tools

INSERT INTO bookings (user_id, book_id) VALUES
((SELECT id FROM users WHERE email='alice@example.com'), (SELECT id FROM books WHERE title='Foundation')),
((SELECT id FROM users WHERE email='bob@example.com'), (SELECT id FROM books WHERE title='Harry Potter and the Sorcerer''s Stone')),
((SELECT id FROM users WHERE email='charlie@example.com'), (SELECT id FROM books WHERE title='A Game of Thrones')),
((SELECT id FROM users WHERE email='diana@example.com'), (SELECT id FROM books WHERE title='Murder on the Orient Express')),
((SELECT id FROM users WHERE email='ethan@example.com'), (SELECT id FROM books WHERE title='I, Robot')),
((SELECT id FROM users WHERE email='fiona@example.com'), (SELECT id FROM books WHERE title='Rendezvous with Rama')),
((SELECT id FROM users WHERE email='george@example.com'), (SELECT id FROM books WHERE title='And Then There Were None')),
((SELECT id FROM users WHERE email='hannah@example.com'), (SELECT id FROM books WHERE title='Foundation')),
((SELECT id FROM users WHERE email='ian@example.com'), (SELECT id FROM books WHERE title='Childhood''s End')),
((SELECT id FROM users WHERE email='julia@example.com'), (SELECT id FROM books WHERE title='Harry Potter and the Sorcerer''s Stone'));
