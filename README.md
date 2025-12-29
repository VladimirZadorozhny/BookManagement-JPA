# Book Management Web Application - Pet Project

A full-stack Spring Boot and vanilla JavaScript application that provides a web-based UI for a "books market" (library). This project evolved from a console-based application to a modern web app with a RESTful backend and a dynamic, interactive frontend. It is built as a study/practice application to demonstrate a simple layered design, REST API principles, and manual frontend state management.

---

### Features

-   **Full-Stack CRUD:** Complete Create, Read, Update, and Delete operations for Books, Authors, and Users.
-   **Interactive Web UI:** A clean, responsive frontend built with vanilla JavaScript, HTML, and CSS. No frameworks were used on the frontend, demonstrating manual DOM manipulation and state management.
-   **Dynamic Filtering and Searching:** The UI allows users to dynamically list and filter entities, such as finding books by title, author name, or year.
-   **Book Renting/Returning Workflow:** A complete workflow allows users to rent available books and return them, with the UI guiding the user through the process.
-   **Robust Error Handling:** The backend provides specific, user-friendly error messages (e.g., "Cannot delete an author who has books") that are displayed in a custom modal on the frontend.
-   **Client-Side Validation:** Forms include client-side validation to provide immediate feedback to the user.
-   **Efficient Backend:** The backend uses DTOs and optimized queries (`BookDetailDto`) to ensure efficient data transfer.
-   **Robust Concurrency Handling:** The booking system (rent/return) is designed to ensure data integrity under concurrent access scenarios, with specific integration tests to verify its behavior.

### Tech Stack

-   **Backend:**
    -   Java 17 & Spring Boot 3.x
    -   Spring Web (for REST APIs)
    -   Spring Data JDBC (`JdbcClient`) - no ORM
    -   Flyway (for database migrations)
    -   MySQL 8.x (via Docker)
    -   Maven (for dependency management)
-   **Frontend:**
    -   Vanilla JavaScript (ES6 Modules)
    -   HTML5
    -   CSS3
-   **Testing:**
    -   JUnit 5 & Mockito
    -   Spring Boot Test (`@SpringBootTest` with `MockMvc`) for controller integration tests, including specialized tests for concurrency.

---

### Project Layout

```
src/main/
├─ java/org/mystudying/bookmanagementweb/
│  ├─ controller/  # REST API controllers and GlobalExceptionHandler
│  ├─ domain/      # Plain domain objects: User, Author, Book, Booking
│  ├─ dto/         # Data Transfer Objects for API communication
│  ├─ exceptions/  # Custom business logic exceptions
│  ├─ repositories/ # SQL + JdbcClient for database access
│  └─ services/     # Business logic + transactions
└─ resources/
   ├─ application.properties # Main application configuration
   ├─ db/migration/         # Flyway SQL migrations (V1_*, V2_*)
   └─ static/               # Frontend source files (HTML, CSS, JS)
pom.xml
docker-compose.yml
```

---

### How to Run the Application

#### Option A — Run with Docker Compose (recommended)

1.  **Start MySQL in Docker:**
    ```bash
    docker compose up -d
    ```
    This creates a `booksmarket` database with user "user1" and password "user1" on port `3307`.

2.  **Run the Spring Boot application (from project root):**
    You can use the Maven wrapper included with the project.

    *   On Linux/macOS or Windows PowerShell:
        ```bash
        ./mvnw spring-boot:run
        ```
    *   On Windows CMD:
        ```bash
        mvnw.cmd spring-boot:run
        ```
    The application will connect to the MySQL instance running in Docker. Flyway will automatically run migrations and seed data on the first start.

3.  **Access the Web Application:**
    Once the backend is running, open your web browser and navigate to:
    **[http://localhost:8080](http://localhost:8080)**

To stop the database:
```bash
      docker compose down
```

#### Option B — Run against your local MySQL

1.  **Create a database named `booksmarket`** (or choose another name and adjust the URL).

2.  **Set connection parameters** (recommended via environment variables):

    *   Windows PowerShell:
        ```powershell
        $env:DB_URL="jdbc:mysql://localhost:3307/booksmarket"
        $env:DB_USER="user1"
        $env:DB_PASSWORD="user1"
        ```
    
3.  **Run the Spring Boot application:**
    You can use the Maven wrapper included with the project.

    *   On Linux/macOS or Windows PowerShell:
        ```bash
        ./mvnw spring-boot:run
        ```
    *   On Windows CMD:
        ```bash
        mvnw.cmd spring-boot:run
        ```
    Flyway will automatically create tables and insert demo data on the first run.

---

### API Endpoints

The application exposes the following RESTful API endpoints:

#### Books (`/api/books`)
- `GET /`: Lists all books. Supports query parameters for filtering (e.g., `?year=2020`, `?title=...`, `?available=true`).
- `GET /{id}`: Retrieves a single book by its ID.
- `GET /{id}/details`: Retrieves aggregated book details (including author name and availability boolean).
- `POST /`: Creates a new book.
- `PUT /{id}`: Updates an existing book.
- `DELETE /{id}`: Deletes a book.

#### Authors (`/api/authors`)
- `GET /`: Lists all authors.
- `GET /{id}`: Retrieves a single author.
- `POST /`: Creates a new author.
- `PUT /{id}`: Updates an existing author.
- `DELETE /{id}`: Deletes an author.

#### Users (`/api/users`)
- `GET /`: Lists all users.
- `GET /{id}`: Retrieves a single user.
- `POST /`: Creates a new user.
- `PUT /{id}`: Updates an existing user.
- `DELETE /{id}`: Deletes a user.

#### Booking Actions
- `POST /api/users/{id}/rent`: Rents a book to a specific user.
- `POST /api/users/{id}/return`: Returns a book from a specific user.

---

### Running Tests

To run the backend integration tests, use the following command:
```bash
    # On Linux/macOS or Windows PowerShell:
      ./mvnw test

    # On Windows CMD:
      mvnw.cmd test
```
Tests expect a running MySQL instance (e.g., started with `docker compose up -d`).

---
