-- Refactor bookings table completely
CREATE TABLE bookings_temp AS SELECT * FROM bookings;

DROP TABLE bookings;

CREATE TABLE bookings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          book_id BIGINT NOT NULL,

                          borrowed_at DATE NOT NULL,
                          due_at DATE NOT NULL,
                          returned_at DATE,

                          fine DECIMAL(10,2) DEFAULT 0,
                          fine_paid BOOLEAN DEFAULT FALSE,

                          FOREIGN KEY (user_id) REFERENCES users(id),
                          FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Migrate data

INSERT INTO bookings (user_id, book_id, borrowed_at, due_at)
SELECT user_id, book_id, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY) FROM bookings_temp;

DROP TABLE bookings_temp;