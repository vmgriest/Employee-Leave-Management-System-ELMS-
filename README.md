# Employee Leave Management System (ELMS)

A production-ready **Spring Boot 3.x REST API** for managing employee leave requests within an organisation. Employees can apply for leave, managers can approve or reject requests, and admins have full visibility over all data.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Prerequisites](#prerequisites)
4. [Quick Start (H2 In-Memory)](#quick-start-h2-in-memory)
5. [Setup with MySQL](#setup-with-mysql)
6. [Running with Docker Compose](#running-with-docker-compose)
7. [API Endpoints](#api-endpoints)
8. [Project Structure](#project-structure)
9. [Running Tests](#running-tests)
10. [Interview Talking Points](#interview-talking-points)

---

## Project Overview

ELMS provides three roles:

| Role | Capabilities |
|------|-------------|
| **EMPLOYEE** | Apply for leave, view own leaves, cancel pending requests |
| **MANAGER** | All employee capabilities + approve/reject team leave requests |
| **ADMIN** | Full access — can approve/reject any request |

Business rules enforced:
- An employee cannot apply if they already have a pending request.
- Leave balance is checked before submission (default: 20 days/year).
- Start date cannot be in the past; end date must be >= start date.
- Only the direct manager (matched via `managerId`) can approve/reject.
- Balance is deducted only on approval, not on rejection.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.x** (Web, Data JPA, Validation)
- **MySQL 8** (production) / **H2** (testing / quick-start)
- **Hibernate / JPA** with `ddl-auto=update`
- **Lombok** for boilerplate reduction
- **SpringDoc OpenAPI 2.3** (Swagger UI)
- **JUnit 5 + Mockito** for unit tests
- **Docker Compose** for containerised setup

---

## Prerequisites

| Tool | Minimum Version |
|------|----------------|
| Java (JDK) | 17 |
| Maven | 3.8+ |
| MySQL | 8.0 (skip for H2 quick-start) |
| Docker + Docker Compose | 20+ (optional) |

---

## Quick Start (H2 In-Memory)

**H2 is the default database — no installation required.** Just run:

```bash
mvn spring-boot:run
```

Wait for the line:
```
Started ElmsApplication in X.XXX seconds
```

Then open:

| URL | Purpose |
|-----|---------|
| http://localhost:8080/swagger-ui.html | Interactive API explorer |
| http://localhost:8080/h2-console | Database browser |

**H2 Console login:**
- JDBC URL: `jdbc:h2:mem:elmsdb`
- Username: `sa`
- Password: *(leave blank)*

> Data is in-memory and resets every time you restart the app. This is normal for development.

---

## Setup with MySQL

When you're ready to persist data permanently, switch to MySQL.

### 1. Create the database

```sql
CREATE DATABASE elms_db;
CREATE USER 'elms_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON elms_db.* TO 'elms_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Update `application.properties`

Comment out the H2 block and uncomment the MySQL block:

```properties
# Comment these out:
# spring.datasource.url=jdbc:h2:mem:elmsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
# spring.datasource.username=sa
# ...

# Uncomment and fill in these:
spring.datasource.url=jdbc:mysql://localhost:3306/elms_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=elms_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 3. Run

```bash
mvn spring-boot:run
```

Hibernate creates the tables automatically on first run (`ddl-auto=update`).

---

## Running with Docker Compose

```bash
# Build the JAR first
mvn clean package -DskipTests

# Start MySQL + application containers
docker-compose up --build
```

The application will wait for MySQL to be healthy before starting. Access Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

To stop:
```bash
docker-compose down
```

---

## API Endpoints

### Employee Management — `/api/employees`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/employees` | Create a new employee |
| `GET` | `/api/employees` | List all employees |
| `GET` | `/api/employees/{id}` | Get employee by database ID |
| `GET` | `/api/employees/emp-id/{employeeId}` | Get employee by business ID (e.g. TCS001) |
| `GET` | `/api/employees/department/{dept}` | List employees by department |
| `PUT` | `/api/employees/{id}` | Update employee details |
| `DELETE` | `/api/employees/{id}` | Delete an employee |
| `GET` | `/api/employees/{id}/leave-balance` | Get remaining leave balance |

### Leave Management — `/api/leaves`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/leaves/apply/{employeeId}` | Submit a leave application |
| `GET` | `/api/leaves` | List all leave requests (admin) |
| `GET` | `/api/leaves/{id}` | Get a single leave request |
| `GET` | `/api/leaves/employee/{employeeId}` | Get leaves for an employee |
| `GET` | `/api/leaves/pending/manager/{managerId}` | Get pending leaves for a manager's team |
| `PUT` | `/api/leaves/{requestId}/approve/{managerId}` | Approve a leave request |
| `PUT` | `/api/leaves/{requestId}/reject/{managerId}` | Reject a leave request |
| `PUT` | `/api/leaves/{requestId}/cancel/{employeeId}` | Cancel a pending request (employee) |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/tcs/elms/
│   │   ├── ElmsApplication.java          # Application entry point
│   │   ├── config/
│   │   │   └── OpenApiConfig.java        # Swagger / OpenAPI configuration
│   │   ├── controller/
│   │   │   ├── EmployeeController.java   # Employee REST endpoints
│   │   │   └── LeaveController.java      # Leave REST endpoints
│   │   ├── dto/
│   │   │   ├── ApiResponse.java          # Generic API wrapper
│   │   │   ├── EmployeeDto.java          # Employee create/update payload
│   │   │   ├── ErrorResponse.java        # Error payload
│   │   │   └── LeaveRequestDto.java      # Leave application payload
│   │   ├── entity/
│   │   │   ├── Employee.java             # Employee JPA entity
│   │   │   └── LeaveRequest.java         # LeaveRequest JPA entity
│   │   ├── enums/
│   │   │   ├── LeaveStatus.java          # PENDING | APPROVED | REJECTED
│   │   │   ├── LeaveType.java            # CASUAL | SICK | EARNED | EMERGENCY
│   │   │   └── Role.java                 # EMPLOYEE | MANAGER | ADMIN
│   │   ├── exception/
│   │   │   ├── AccessDeniedException.java
│   │   │   ├── BusinessException.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── repository/
│   │   │   ├── EmployeeRepository.java
│   │   │   └── LeaveRequestRepository.java
│   │   └── service/
│   │       ├── EmployeeService.java
│   │       └── LeaveService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/tcs/elms/
        └── service/
            ├── EmployeeServiceTest.java
            └── LeaveServiceTest.java
```

---

## Running Tests

```bash
# Run all tests (uses H2 in-memory — no MySQL needed)
mvn test

# Run a specific test class
mvn test -Dtest=LeaveServiceTest

# Run with coverage report (if JaCoCo is configured)
mvn verify
```

Tests use **Mockito** (`@ExtendWith(MockitoExtension.class)`) so no Spring context is loaded — they run fast and in isolation.

---

## Interview Talking Points

### Architecture & Design Patterns
- **Layered Architecture**: Controller → Service → Repository separation of concerns.
- **DTO Pattern**: `EmployeeDto` / `LeaveRequestDto` decouple API contracts from JPA entities, preventing over-posting and circular serialisation.
- **Generic API Wrapper**: `ApiResponse<T>` provides a consistent response envelope across all endpoints.
- **Global Exception Handler**: `@RestControllerAdvice` centralises error handling, mapping domain exceptions to HTTP status codes cleanly.

### Spring Boot Features Used
- **Spring Data JPA**: Repository abstraction with derived query methods and custom `@Query` JPQL.
- **Bean Validation (JSR-380)**: `@Valid` on controller methods + `@NotBlank`, `@NotNull`, `@Email` on DTOs.
- **`@Transactional`**: Read-only by default at class level; write methods override with full transactions, ensuring data consistency.
- **SpringDoc OpenAPI**: Auto-generates interactive Swagger UI from annotations.

### Database Design Decisions
- `managerId` on `Employee` is stored as a plain `Long` (not a `@ManyToOne` self-join) to avoid bidirectional lazy-loading complexity and circular JSON serialisation.
- `LeaveRequest.employee` uses `FetchType.LAZY` to avoid N+1 query problems.
- `@Builder.Default` on `availableLeaves` and `status` ensures Lombok builder sets correct defaults.

### Testing Strategy
- **Unit tests with Mockito**: All dependencies mocked — tests run without a Spring context or database, giving millisecond feedback.
- **Test coverage areas**: happy path, boundary conditions (insufficient balance, past dates), and security checks (wrong role, wrong manager).
- **H2 scope**: H2 is on `runtime` scope so it works for both local development (`spring-boot:run`) and unit tests. Switching between H2 and MySQL requires only a properties change — no code changes.

### Potential Enhancements (for discussion)
- **Spring Security + JWT**: Add token-based authentication; role checks currently rely on path parameters.
- **Leave accrual scheduler**: `@Scheduled` job to reset/accrue leave balances annually.
- **Audit logging**: Spring Data Envers or a custom `@EntityListeners` for change history.
- **Pagination**: `Pageable` on list endpoints for large datasets.
- **Email notifications**: `JavaMailSender` to notify employees/managers on status changes.
