# UIDAI Meetings & Minutes of Meeting (MoM) Governance Module

A Spring Boot service that provides UIDAI with a structured, auditable system for
capturing meeting outcomes, assigning and tracking action items, and ensuring
accountability across governance, steering, and migration meetings involving
UIDAI, PMC and MSP stakeholders.

## Tech stack

| Concern        | Choice                                          |
|----------------|-------------------------------------------------|
| Language       | Java 21                                         |
| Framework      | Spring Boot 3.4                                  |
| Build          | Maven (or Docker)                               |
| Database       | PostgreSQL 16 (H2 in tests)                     |
| Migrations     | Flyway                                          |
| API docs       | springdoc-openapi (Swagger UI) at `/meetings/docs` |
| External APIs  | User, Project & Activity services (Python, HTTP) |
| Auth           | Bearer token (forwarded to the external services) |

The app runs under the `/meetings` context-path on port `8080`
(e.g. `http://localhost:8080/meetings/...`).

## Building & running

### With Maven

Java 21 must be installed (`java -version`). Maven is **not** required to be on
your PATH — the bundled `build.ps1` downloads a project-local Maven on first use.

```powershell
.\build.ps1                  # clean package
.\build.ps1 test             # run tests (uses in-memory H2)
.\build.ps1 spring-boot:run  # run the app
```

If you already have Maven installed you can use `mvn` directly instead.

### With Docker

```powershell
docker build -t meetings-mom-governance:latest .
docker run --rm -p 8080:8080 meetings-mom-governance:latest
# or, via compose (also offers a local postgres:16 service):
docker compose up --build app
```

## Configuration

### Database

The module targets **PostgreSQL 16**. Connection settings are read from
environment variables (defaults live in `src/main/resources/application.properties`):

```
DB_URL=jdbc:postgresql://<host>:5432/<db>
DB_USERNAME=<user>
DB_PASSWORD=<password>
```

- Flyway applies the migrations in `src/main/resources/db/migration` (V1–V6) on
  startup. They are **idempotent** (`CREATE TABLE/INDEX IF NOT EXISTS`,
  `ADD COLUMN IF NOT EXISTS`, `ON CONFLICT DO NOTHING`).
- `spring.flyway.baseline-version=0` lets the module's tables coexist in an
  already-populated (shared) `public` schema.
- A pinned local PostgreSQL 16 is available for development:

  ```powershell
  docker compose up -d postgres   # localhost:5432, db/user/pass = postgres
  ```

### Authentication & external services

Every endpoint requires an `Authorization: Bearer <token>` header. The token is
forwarded as-is to the external Python services, so the caller's identity flows
through to them.

```
USER_SERVICE_URL=http://10.1.131.199/users/api/v3
ACTIVITY_SERVICE_URL=http://10.1.131.199/projects/api/v3
```

- **User service** — validates meeting attendees: `GET /users/{id}` (an attendee
  is valid only if the user exists and is `active`). See
  `external/user/UserServiceRestClient.java`.
- **Project service** — `GET /projects/{id}` resolves the project's
  `meetingMilestoneId`, used as the milestone for the meeting's activity.
- **Activity service** — `POST /milestones/{milestoneId}/activities/create`
  creates the project activity for a meeting; `GET`/`PATCH /activities/{id}`.
  See `external/activity/ActivityServiceRestClient.java`.

External calls can be turned off for standalone/local runs:
`USER_SERVICE_ENABLED=false`, `ACTIVITY_SERVICE_ENABLED=false`.

## API surface

All endpoints are served under the `/meetings` context-path and require the
`Authorization: Bearer` header.

### Meetings (MEET-FR-01 .. FR-03)

| Method & path                          | Purpose                                   |
|----------------------------------------|-------------------------------------------|
| `POST /meetings/create`                | Record a meeting (creates a linked activity) |
| `GET  /meetings/getAll`                | Filter/report meetings (paged)            |
| `GET  /meetings/get/{id}`              | Get a meeting by id                       |
| `PUT  /meetings/update/{id}`           | Update a meeting (and attendance)         |
| `PUT  /meetings/updateStatus/{id}?status=` | Transition a meeting's status         |

`GET /meetings/getAll` supports `projectId`, `status`, `from`, `to`, `page`,
`size`. Pass `ALL` (or omit) for `projectId`/`status` to skip that filter, e.g.
`/meetings/getAll?projectId=ALL&status=ALL&page=0&size=20`.

### Other modules

| Area                | Base path                              | Requirements              |
|---------------------|----------------------------------------|---------------------------|
| Meeting types       | `/meetings/meeting-types`              | MEET-FR-02.4              |
| Minutes of Meeting  | `/meetings/meetings/{meetingId}/mom`, `/meetings/mom/{momId}` | MEET-FR-04, FR-05 |
| Action items        | `/meetings/action-items`               | MEET-FR-04.2/.4/.5        |
| MoM templates       | `/meetings/mom-templates`              | MEET-FR-05.1              |
| Audit log           | `/meetings/audit-logs`                 | MEET-FR-01.3, FR-04.5     |

Swagger UI: `http://localhost:8080/meetings/docs`

### Creating a meeting

`POST /meetings/create` — no `milestoneId` is sent; the milestone is resolved
automatically from the project's `meetingMilestoneId`.

```json
{
  "title": "Quarterly governance sync",
  "meetingDate": "2026-06-10",
  "startTime": "09:00",
  "endTime": "10:30",
  "description": "Review milestones and risks",
  "meetingLink": "https://meet.example.com/abc",
  "projectId": "dbbff5f8-a814-4c2e-9a86-82a8f83bdea9",
  "attendees": [
    { "userId": "ead6aaed-...", "participantRole": "Chair", "mandatory": true, "isPresent": false }
  ],
  "externalAttendees": [ { "email": "guest@vendor.com", "isPresent": false } ],
  "attachments": [ "doc1.pdf" ]
}
```

On create the service: validates `attendees` against the User service (external
attendees are stored as-is, not validated), looks up the project to resolve the
milestone, creates a project activity (name/description/start/end derived from the
meeting, times interpreted in IST), and links the activity ids back onto the
meeting. `attendees`/`externalAttendees` carry an `isPresent` flag for attendance;
`attachments` is forwarded to the activity only when non-empty.

## Requirements traceability

See the Javadoc on each controller/service method — every method is annotated
with the `MEET-FR-xx` requirement it satisfies.
