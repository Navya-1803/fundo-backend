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
| RabbitMQ (AMQP) | Due reminders queued for emailing |
| Redis | OTPs for password reset and the notes cache |
| Spring Mail (SMTP) | Emailing the password reset OTP and note reminders |
| Spring Batch + Apache POI | Trash cleanup job and Excel note import |
| Spring AOP | Method logging and performance timing |
| SLF4J / Logback | Logging |
| JUnit 5, Mockito, MockMvc, H2 | Tests |
| Docker Compose | Starting PostgreSQL, Redis, RabbitMQ, ActiveMQ and Mailpit |

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
| `feature/UC1-UserEntityRepository` | User entity and repository |
| `feature/UC2-DTO` | Request / response DTOs with validation |
| `feature/UC3-ServiceController` | Register and login endpoints |
| `feature/UC4-TokenGenAndEncoding` | JWT tokens and BCrypt passwords |
| `feature/UC5-ExceptionHandling` | Custom exceptions and global error handler |
| `feature/UC6-NoteEntityAndRepository` | Note entity and repository |
| `feature/UC7-Note` | Create, list, pin, archive and trash notes |
| `feature/UC8-LoggingStatement` | Logging across the application |
| `feature/UC9-JMSIntegration` | Registration events through ActiveMQ |
| `feature/UC10-RabbitMQ` | Reminder events through RabbitMQ |
| `feature/UC11-RedisImplementation` | Redis key-value API and OTP service |
| `feature/UC12-Cache` | Redis cache for the notes dashboard |
| `feature/UC13-BatchProcessing` | Batch job deleting old trashed notes |
| `feature/UC14-ExcelIntegration` | Import notes from Excel |
| `feature/UC15-AOP` | Logging and performance aspects |
| `feature/UC16-CorsConfiguration` | CORS for the React frontend |
| `feature/UC17-NoteUpdateAndDelete` | Edit, delete forever, archive / trash lists, search |
| `feature/UC18-ReminderPersistence` | Reminders stored on notes, list and remove |
| `feature/UC19-Label` | Labels and labelling notes |
| `feature/UC20-ForgotResetPassword` | Forgot / reset password with OTP |
| `feature/UC21-DockerSetup` | Docker Compose and cache fallback |
| `feature/UC22-EmailOtp` | Email the password reset OTP |
| `feature/UC23-ReminderEmail` | Email reminders when their time is reached |

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
RabbitMQ (5672, web page http://localhost:15672 guest/guest), ActiveMQ
(61616, web console http://localhost:8161 admin/admin) and **Mailpit**, a test
email inbox: every email the backend sends (the password reset OTP and note
reminders) shows up on
http://localhost:8025.

### Reminder emails

When a reminder is set on a note, a scheduler checks every minute
(`app.reminder.check-interval-ms`) for reminders whose time has come. Each due
reminder is put on RabbitMQ and the consumer emails it to the note's owner,
with the note title, description and the optional reminder message. If RabbitMQ
is down the email is sent directly. A reminder is emailed only once
(`reminderSent` becomes `true`); setting a new time sends a new email.

### Sending real emails (optional)

By default emails go to the Mailpit test inbox. To deliver the OTP and reminders to real mailboxes,
set these environment variables before starting the backend (example for Gmail, which
needs 2-Step Verification and an App Password from https://myaccount.google.com/apppasswords):

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your.address@gmail.com
MAIL_PASSWORD=your-16-character-app-password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_FROM=your.address@gmail.com
```

The email password is never stored in the repository.

**Without Docker:** install PostgreSQL, create the database once with
`CREATE DATABASE fundoo_notes;` and run Redis, RabbitMQ and ActiveMQ with their
default settings. If Redis is not running the app still works (the cache is
skipped), only forgot password needs Redis and a mail server (Mailpit or SMTP).

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
| POST | `/api/users/forgot-password` | Email a 6 digit OTP to the user |
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
