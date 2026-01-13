-- ============================================================
-- R__seed_demo_bookings_ui.sql
-- Demo bookings for frontend testing
-- Covers:
--  - active bookings
--  - overdue bookings
--  - returned bookings
--  - fines (paid & unpaid)
-- ============================================================

-- ------------------------------------------------------------
-- CLEANUP (only demo users!)
-- ------------------------------------------------------------

DELETE FROM bookings
WHERE user_id IN (
    SELECT id FROM users
    WHERE email IN (
                    'alice@example.com',
                    'bob@example.com',
                    'charlie@example.com',
                    'diana@example.com',
                    'ethan@example.com',
                    'fiona@example.com',
                    'george@example.com',
                    'hannah@example.com',
                    'ian@example.com'
        )
);

-- ============================================================
-- ACTIVE (NOT OVERDUE) BOOKINGS
-- ============================================================

INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid)
VALUES
    (
        (SELECT id FROM users WHERE email='alice@example.com'),
        (SELECT id FROM books WHERE title='Foundation'),
        CURRENT_DATE - INTERVAL 2 DAY,
        CURRENT_DATE + INTERVAL 12 DAY,
        NULL,
        0,
        FALSE
    ),
    (
        (SELECT id FROM users WHERE email='bob@example.com'),
        (SELECT id FROM books WHERE title='Harry Potter and the Sorcerer''s Stone'),
        CURRENT_DATE - INTERVAL 5 DAY,
        CURRENT_DATE + INTERVAL 9 DAY,
        NULL,
        0,
        FALSE
    );

-- ============================================================
-- ACTIVE (DUE SOON) BOOKINGS
-- ============================================================

INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid)
VALUES
    (
        (SELECT id FROM users WHERE email='ian@example.com'),
        (SELECT id FROM books WHERE title='Foundation'),
        CURRENT_DATE - INTERVAL 12 DAY,
        CURRENT_DATE + INTERVAL 2 DAY,
        NULL,
        0,
        FALSE
    );

-- ============================================================
-- OVERDUE BOOKINGS (NOT RETURNED)
-- ============================================================

INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid)
VALUES
    (
        (SELECT id FROM users WHERE email='charlie@example.com'),
        (SELECT id FROM books WHERE title='A Game of Thrones'),
        CURRENT_DATE - INTERVAL 30 DAY,
        CURRENT_DATE - INTERVAL 16 DAY,
        NULL,
        0,
        FALSE
    ),
    (
        (SELECT id FROM users WHERE email='diana@example.com'),
        (SELECT id FROM books WHERE title='Murder on the Orient Express'),
        CURRENT_DATE - INTERVAL 20 DAY,
        CURRENT_DATE - INTERVAL 6 DAY,
        NULL,
        6,
        FALSE
    ),
    (
        (SELECT id FROM users WHERE email='charlie@example.com'),
        (SELECT id FROM books WHERE title='Murder on the Orient Express'),
        CURRENT_DATE - INTERVAL 20 DAY,
        CURRENT_DATE - INTERVAL 6 DAY,
        NULL,
        0,
        FALSE
    ),
    (
        (SELECT id FROM users WHERE email='diana@example.com'),
        (SELECT id FROM books WHERE title='A Game of Thrones'),
        CURRENT_DATE - INTERVAL 40 DAY,
        CURRENT_DATE - INTERVAL 26 DAY,
        NULL,
        26,
        FALSE
    );

-- ============================================================
-- RETURNED LATE (FINE PAID)
-- ============================================================

INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid)
VALUES
    (
        (SELECT id FROM users WHERE email='ethan@example.com'),
        (SELECT id FROM books WHERE title='I, Robot'),
        CURRENT_DATE - INTERVAL 18 DAY,
        CURRENT_DATE - INTERVAL 4 DAY,
        CURRENT_DATE - INTERVAL 1 DAY,
        3,
        TRUE
    ),
    (
        (SELECT id FROM users WHERE email='fiona@example.com'),
        (SELECT id FROM books WHERE title='Rendezvous with Rama'),
        CURRENT_DATE - INTERVAL 22 DAY,
        CURRENT_DATE - INTERVAL 8 DAY,
        CURRENT_DATE - INTERVAL 3 DAY,
        5,
        TRUE
    );

-- ============================================================
-- RETURNED LATE (FINE UNPAID)
-- ============================================================

INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid)
VALUES
    (
        (SELECT id FROM users WHERE email='ethan@example.com'),
        (SELECT id FROM books WHERE title='A Game of Thrones'),
        CURRENT_DATE - INTERVAL 20 DAY,
        CURRENT_DATE - INTERVAL 6 DAY,
        CURRENT_DATE - INTERVAL 1 DAY,
        5,
        FALSE
    );

-- ============================================================
-- RETURNED ON TIME
-- ============================================================

INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid)
VALUES
    (
        (SELECT id FROM users WHERE email='george@example.com'),
        (SELECT id FROM books WHERE title='And Then There Were None'),
        CURRENT_DATE - INTERVAL 10 DAY,
        CURRENT_DATE - INTERVAL 1 DAY,
        CURRENT_DATE - INTERVAL 1 DAY,
        0,
        FALSE
    ),
    (
        (SELECT id FROM users WHERE email='hannah@example.com'),
        (SELECT id FROM books WHERE title='Childhood''s End'),
        CURRENT_DATE - INTERVAL 7 DAY,
        CURRENT_DATE + INTERVAL 7 DAY,
        CURRENT_DATE - INTERVAL 2 DAY,
        0,
        FALSE
    );

-- ============================================================
-- OVERDUE + UNPAID FINE
-- ============================================================

INSERT INTO bookings (user_id, book_id, borrowed_at, due_at, returned_at, fine, fine_paid)
VALUES
    (
        (SELECT id FROM users WHERE email='ian@example.com'),
        (SELECT id FROM books WHERE title='And Then There Were None'),
        CURRENT_DATE - INTERVAL 18 DAY,
        CURRENT_DATE - INTERVAL 4 DAY,
        NULL,
        3,
        FALSE
    );