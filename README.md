# UIDAI Meetings & Minutes of Meeting (MoM) Governance Module

A Spring Boot service that provides UIDAI with a structured, auditable system for
capturing meeting outcomes, assigning and tracking action items, and ensuring
accountability across governance, steering, and migration meetings involving
UIDAI, PMC and MSP stakeholders.

## Tech stack

| Concern        | Choice                                  |
|----------------|-----------------------------------------|
| Language       | Java 21                                 |
| Framework      | Spring Boot 3.4                          |
| Build          | Maven                                   |
| Database       | PostgreSQL 16 (H2 in tests)             |
| Migrations     | Flyway                                  |
| API docs       | springdoc-openapi (Swagger UI)          |
| External APIs  | User & Activity services (Python, HTTP) |

## Building & running

Java 21 must be installed (`java -version`). Maven is **not** required to be on
your PATH — the bundled `build.ps1` downloads a project-local Maven on first use.

```powershell
.\build.ps1                  # clean package
.\build.ps1 test             # run tests (uses in-memory H2)
.\build.ps1 spring-boot:run  # run the app against PostgreSQL
```

If you already have Maven installed you can use `mvn` directly instead.

### Database

The module targets **PostgreSQL 16**. A pinned local instance is provided via
Docker Compose (matches the application defaults below):

```powershell
docker compose up -d     # start PostgreSQL 16 on localhost:5432
docker compose down      # stop (data kept); add -v to wipe the volume
```

Connection settings (environment variables, defaults shown):

```
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

Flyway runs the schema migrations in `src/main/resources/db/migration` on startup.

### External Python services

The **User** and **Activity** APIs are external Python services consumed over
HTTP. Their concrete endpoint signatures are wired in two client classes and
configured here:

```
USER_SERVICE_URL=http://localhost:9001
ACTIVITY_SERVICE_URL=http://localhost:9002
```

- `external/user/UserServiceRestClient.java` — validates participants against
  authorized roles and resolves user details (MEET-FR-03.2).
- `external/activity/ActivityServiceRestClient.java` — publishes activity/audit
  events for meetings, MoMs and action items.

> **TODO (awaiting API signatures):** the request/response paths and payloads in
> these two clients are placeholders. Once you share the Python API signatures,
> update the `@param`-marked sections and the DTOs in each `dto` sub-package.

## API surface (high level)

All endpoints are served under the `/meetings` context-path.

| Area                | Base path                          | Requirements              |
|---------------------|------------------------------------|---------------------------|
| Meeting types       | `/meetings/meeting-types`          | MEET-FR-02.4              |
| Meetings            | `/meetings/meetings`               | MEET-FR-01, FR-02, FR-03  |
| Minutes of Meeting  | `/meetings/meetings/{id}/mom`      | MEET-FR-04, FR-05         |
| Action items        | `/meetings/action-items`           | MEET-FR-04.2/.4/.5        |
| MoM templates       | `/meetings/mom-templates`          | MEET-FR-05.1              |
| Audit log           | `/meetings/audit-logs`             | MEET-FR-01.3, FR-04.5     |

Swagger UI: `http://localhost:8080/meetings/docs`

## Requirements traceability

See the Javadoc on each controller/service method — every method is annotated
with the `MEET-FR-xx` requirement it satisfies.
