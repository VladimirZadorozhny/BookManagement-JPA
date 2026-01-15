# Book Management Application (Spring Boot + JPA)

A full-stack **Spring Boot + JPA** pet project with a **vanilla JavaScript frontend** that models a real-world library / book-renting system.
The application evolved through multiple architectural stages (Console → Web → JDBC → JPA) and now focuses on **correct domain modeling, performance-aware JPA usage, and clean REST design**.

This project is intentionally **not over-simplified**: it contains real-life concerns such as booking workflows, overdue handling, fines, reporting, pagination, and N+1 query prevention.

---

## Key Goals of the Project

* Practice **Spring Boot + JPA** in a realistic domain
* Learn **fetch strategies**, `JOIN FETCH`, pagination, and N+1 avoidance
* Build a **non-framework frontend** with explicit state handling
* Design a system that remains **testable and evolvable**
* Simulate real backend complexity (reports, aggregates, workflows)

---

## Features

### Core Domain

* **Books, Authors, Genres, Users** (full CRUD)
* **Booking system** instead of direct user–book relations
* Explicit **rent / return workflow**
* Support for:

    * due dates
    * overdue detection
    * fines & fine payment

### Booking & Business Logic

* Rent only if book is available
* Prevent renting if user has unpaid fines or overdue books
* Calculate overdue days and fines dynamically
* Keep booking history (active + returned)

### Admin / Reporting

* Booking reports with pagination:

    * all bookings
    * active bookings
    * returned bookings
    * overdue & due-soon bookings
    * bookings with fines / unpaid fines
    * "heavy users" (users with more than X active bookings)
* Efficient queries using **JOIN FETCH + count queries**

### Frontend

* Pure **vanilla JavaScript (ES modules)**
* Manual DOM updates & state handling
* Filter panels and paginated tables
* Visual status indicators (overdue, near-due, unpaid fines)
* Custom modal dialogs for confirmations & errors

### Performance & Data Access

* DTO-based API design
* No lazy-loading surprises in controllers
* Explicit fetch strategies for each use case
* Final **N+1 sanity pass completed** across the application

---

## Tech Stack

### Backend

* Java 17
* Spring Boot 3.x
* Spring Web (REST API)
* Spring Data JPA (Hibernate)
* Flyway (DB migrations + repeatable seed scripts)
* MySQL 8 (Docker)
* Maven

### Frontend

* HTML5
* CSS3
* Vanilla JavaScript (ES6 modules)

### Testing

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc
* Integration tests for booking logic

---

## Project Structure

```
src/main/
├─ java/org/mystudying/bookmanagementjpa/
│  ├─ controller/      # REST controllers
│  ├─ domain/          # JPA entities (Book, Author, Genre, User, Booking)
│  ├─ dto/             # API DTOs (requests + responses)
│  ├─ exception/       # Custom business exceptions + handlers
│  ├─ repository/      # Spring Data JPA repositories
│  └─ service/         # Transactional business logic
└─ resources/
   ├─ application.properties
   ├─ db/migration/    # Flyway versioned & repeatable scripts
   └─ static/          # Frontend (HTML, CSS, JS)

pom.xml
docker-compose.yml
```

---

## Running the Application

### Option A — Docker (recommended)

1. Start MySQL:

```bash
docker compose up -d
```

2. Run the application:

```bash
./mvnw spring-boot:run
```

3. Open browser:

```
http://localhost:8080
```

Flyway will automatically create the schema and insert demo data.

To stop:

```bash
docker compose down
```

---

### Option B — Local MySQL

Set environment variables:

```bash
DB_URL=jdbc:mysql://localhost:3307/booksmarket
DB_USER=user1
DB_PASSWORD=user1
```

Then run:

```bash
./mvnw spring-boot:run
```

---

## REST API Overview

### Books (`/api/books`)

* `GET /api/books`
* `GET /api/books/{id}`
* `POST /api/books`
* `PUT /api/books/{id}`
* `DELETE /api/books/{id}`

### Authors (`/api/authors`)

* Standard CRUD endpoints

### Genres (`/api/genres`)

* List genres
* Retrieve books by genre (optimized fetch)

### Users (`/api/users`)

* CRUD operations
* Booking-related actions

### Booking Actions

* `POST /api/users/{id}/rent`
* `POST /api/users/{id}/return`
* `POST /api/users/{id}/bookings/{bookingId}/pay`

### Reports (`/api/reports/bookings`)

* Supports pagination and multiple report types via query params

---

## Testing

Run all tests:

```bash
./mvnw test
```

Tests expect a running MySQL instance (Docker recommended).

---

## Project Status & Roadmap

**Current state:** Feature-complete, performance-stable, test-passing baseline.

Planned future phases:

* Authentication & Roles (Spring Security)
* Swagger / OpenAPI documentation
* Environment-based profiles
* Email notifications
* Further report refinements
* Possible microservice split (Docker / Kubernetes)

---

## Author

**Volodymyr Zadorozhnyi**

Junior Java Backend Developer

(Spring Boot • JPA • SQL • Docker)

This project represents a continuous learning journey and a realistic backend portfolio piece.
