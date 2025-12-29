set names utf8mb4;
set charset utf8mb4;

drop database if exists booksMarket;
create database booksMarket charset utf8mb4;
use booksMarket;

-- Drop existing tables (optional for dev resets)
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS authors;
DROP TABLE IF EXISTS users;

-- =====================
-- TABLE DEFINITIONS
-- =====================

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE authors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birthdate DATE
);

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    year INT,
    author_id INT,
    available INT DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES authors(id)
);

CREATE TABLE bookings (
    user_id INT,
    book_id INT,
    PRIMARY KEY (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- =====================
-- SAMPLE DATA
-- =====================

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
('Foundation', 1951, 1, 3),
('Harry Potter and the Sorcerer\'s Stone', 1997, 2, 5),
('A Game of Thrones', 1996, 3, 0),
('Childhood\'s End', 1953, 4, 2),
('Murder on the Orient Express', 1934, 5, 4),
('I, Robot', 1950, 1, 1),
('Harry Potter and the Chamber of Secrets', 1998, 2, 2),
('A Clash of Kings', 1998, 3, 0),
('Rendezvous with Rama', 1973, 4, 3),
('And Then There Were None', 1939, 5, 5);

-- BOOKINGS (users borrowing books)
INSERT INTO bookings (user_id, book_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 5),
(5, 6),
(6, 9),
(7, 10),
(8, 1),
(9, 4),
(10, 2);

create user if not exists user1 identified by 'user1';
grant select,insert,update, delete on users to user1;
grant select,insert,update, delete on authors to user1;
grant select,insert,update, delete on books to user1;
grant select,insert,update, delete on users to bookings;
