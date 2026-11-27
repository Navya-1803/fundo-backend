# Fundoo Notes Backend

Spring Boot REST API for **Fundoo Notes**, a Google Keep style notes application.
Users register, sign in with a JWT and manage their own notes: create, edit, pin,
archive, trash, delete forever, search, label and set reminders.
The React frontend lives in the separate repository
[fundo-frontend](https://github.com/Navya-1803/fundo-frontend).

> The code is on the **`develop`** branch. `main` only holds this README.

## Tech Stack

| Technology | Used for |
|---|---|
| Java 21, Spring Boot 4.1 | Application framework |
| Spring Web MVC | REST API |
| Spring Data JPA / Hibernate | Persistence |
| **PostgreSQL** | Database |
| Jakarta Validation | Request validation |
| JJWT, BCrypt (Spring Security Crypto) | Login tokens and password hashing |
| ActiveMQ (JMS) | Welcome email event after registration |
| RabbitMQ (AMQP) | Reminder notification events |
| Redis | OTPs for password reset and the notes cache |
| Spring Batch + Apache POI | Trash cleanup job and Excel note import |
| Spring AOP | Method logging and performance timing |
| SLF4J / Logback | Logging |
| JUnit 5, Mockito, MockMvc, H2 | Tests |
| Docker Compose | Starting PostgreSQL, Redis, RabbitMQ and ActiveMQ |

## Project Structure

```
src/main/java/com/fundoo/notes
├── aspect        logging and performance aspects
├── config        password encoder, CORS, JMS, RabbitMQ, Redis, cache and batch configuration
├── controller    REST controllers
├── dto
│   ├── request   request bodies with validation
│   └── response  response bodies and error format
├── entity        JPA entities (User, Note, Label)
├── event         messages sent through JMS and RabbitMQ
├── exception     custom exceptions and global exception handler
├── repository    Spring Data JPA repositories
├── service       service interfaces
│   └── impl      service implementations, publishers and consumers
└── util          entity to DTO mapping
```

## Feature Branches

Every feature was built on its own branch from `develop` and merged back in this order:

| Branch | Feature |
|---|---|
| `feature/UserEntityRepository` | User entity and repository |
| `feature/DTO` | Request / response DTOs with validation |
| `feature/ServiceController` | Register and login endpoints |
| `feature/TokenGenAndEncoding` | JWT tokens and BCrypt passwords |
| `feature/ExceptionHandling` | Custom exceptions and global error handler |
| `feature/NoteEntityAndRepository` | Note entity and repository |
| `feature/Note` | Create, list, pin, archive and trash notes |
| `feature/LoggingStatement` | Logging across the application |
| `feature/JMSIntegration` | Registration events through ActiveMQ |
| `feature/RabbitMQ` | Reminder events through RabbitMQ |
| `feature/RedisImplementation` | Redis key-value API and OTP service |
| `feature/Cache` | Redis cache for the notes dashboard |
| `feature/BatchProcessing` | Batch job deleting old trashed notes |
| `feature/ExcelIntegration` | Import notes from Excel |
| `feature/AOP` | Logging and performance aspects |
| `feature/CorsConfiguration` | CORS for the React frontend |
| `feature/NoteUpdateAndDelete` | Edit, delete forever, archive / trash lists, search |
| `feature/ReminderPersistence` | Reminders stored on notes, list and remove |
| `feature/Label` | Labels and labelling notes |
| `feature/ForgotResetPassword` | Forgot / reset password with OTP |
| `feature/DockerSetup` | Docker Compose and cache fallback |

## How to Run

### 1. Requirements

* **Java 21** (JDK) - check with `java -version`
* **Docker Desktop** (easiest) for PostgreSQL, Redis, RabbitMQ and ActiveMQ,
  or install those four services yourself
* Git

Maven is not needed, the project contains the Maven wrapper (`mvnw`).

### 2. Get the code

```bash
git clone https://github.com/Navya-1803/fundo-backend.git
cd fundo-backend
git checkout develop
```

### 3. Start the services

Create a file called `.env` next to `docker-compose.yml` (copy `.env.example`) and put
your PostgreSQL password in it:

```
DB_USERNAME=postgres
DB_PASSWORD=your-password
```

Then start everything:

```bash
docker compose up -d
```

This starts PostgreSQL (5432, database `fundoo_notes`), Redis (6379),
RabbitMQ (5672, web page http://localhost:15672 guest/guest) and ActiveMQ
(61616, web console http://localhost:8161 admin/admin).

**Without Docker:** install PostgreSQL, create the database once with
`CREATE DATABASE fundoo_notes;` and run Redis, RabbitMQ and ActiveMQ with their
default settings. If Redis is not running the app still works (the cache is
skipped), only forgot password needs Redis.

### 4. Start the backend

The database password is read from the `DB_PASSWORD` environment variable, it is not
stored in the repository.

macOS / Linux:

```bash
export DB_PASSWORD=your-password
./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
$env:DB_PASSWORD="your-password"
.\mvnw.cmd spring-boot:run
```

The API runs on http://localhost:8080. Tables are created automatically on the first
start. Check it with http://localhost:8080/actuator/health.

Optional environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`,
`JWT_SECRET`, `CORS_ALLOWED_ORIGINS` (default `http://localhost:5173`).

### 5. Run the tests

```bash
./mvnw test
```

Tests use an H2 in-memory database and mocks, no service needs to be running.

## REST API

Every endpoint except `/api/users/**` needs the header `Authorization: Bearer <token>`.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register |
| POST | `/api/users/login` | Log in, returns a JWT |
| POST | `/api/users/forgot-password` | Get an OTP (returned because no mail server is set up) |
| POST | `/api/users/reset-password` | Reset password with the OTP |
| GET | `/api/notes` | Active notes, pinned first |
| POST | `/api/notes` | Create a note |
| PUT | `/api/notes/{id}` | Edit a note |
| DELETE | `/api/notes/{id}` | Delete forever (only from trash) |
| GET | `/api/notes/archived` | Archived notes |
| GET | `/api/notes/trash` | Notes in trash |
| GET | `/api/notes/search?keyword=` | Search notes |
| PATCH | `/api/notes/{id}/pin` `/archive` `/trash` | Toggle pin, archive, trash |
| POST / DELETE | `/api/notes/{id}/labels/{labelId}` | Add / remove a label |
| GET / POST | `/api/labels` | List / create labels |
| PUT / DELETE | `/api/labels/{id}` | Rename / delete a label |
| GET | `/api/labels/{id}/notes` | Notes with a label |
| GET / POST | `/api/reminders` | List / set reminders |
| DELETE | `/api/reminders/{noteId}` | Remove a reminder |
| POST | `/api/batch/run?retentionDays=` | Run the trash cleanup job |
| POST | `/api/batch/import/notes` | Import notes from `.xlsx` (form field `file`) |
| POST / GET / DELETE | `/api/redis/...` | Redis demo endpoints |

Errors always look like:

```json
{"status": 404, "error": "Not Found", "message": "Note not found with id: 9", "path": "/api/notes/9/pin", "timestamp": "..."}
```

## Author

Navya Raj
