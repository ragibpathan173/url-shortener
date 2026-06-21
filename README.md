# Ragib URL Shortener

A full-stack Spring Boot URL shortener with user accounts, private links, click tracking, and Docker support.

## What It Does
- Shortens long URLs into compact shareable links
- Supports guest link creation
- Lets signed-in users create private links and set expiry dates
- Tracks click counts
- Includes an admin dashboard for managing all links
- Provides a responsive cinematic Thymeleaf UI with glass-style cards and a video hero

## Tech Stack
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- H2 and PostgreSQL
- Docker

## Run Locally

```bash
cd spring-boot-url-shortener
mvn spring-boot:run
```

Open `http://localhost:8080`

## Mobile Preview

After starting the app, open `http://localhost:8080` in Chrome or Edge.

Use the browser device toolbar to preview the responsive layout:

```text
F12 -> Ctrl + Shift + M -> choose a phone viewport -> Ctrl + F5
```

Local accounts and links are stored in `data/url-shortener` and survive restarts.
That folder is intentionally ignored by Git. To start with a blank local database,
stop the app and delete the `data` folder.

## PostgreSQL Profile

The application can also run with PostgreSQL by enabling the `postgres` profile.
Connection details come from environment variables, so passwords are not committed:

```bash
SPRING_PROFILES_ACTIVE=postgres \
DATABASE_URL=jdbc:postgresql://localhost:5432/url_shortener \
DATABASE_USERNAME=url_shortener \
DATABASE_PASSWORD=your_password \
./mvnw spring-boot:run
```

On an empty PostgreSQL database, Flyway automatically creates the application schema.
The PostgreSQL profile validates that schema instead of letting Hibernate change it at runtime.

## Docker

```bash
cd spring-boot-url-shortener
cp .env.example .env
docker compose -f docker/compose.yaml up --build
```

Open `http://localhost:8080`

Docker Compose starts the app with the PostgreSQL profile and keeps database data
in a named Docker volume. The checked-in values are local development defaults;
change `POSTGRES_PASSWORD`, `POSTGRES_PORT`, or `APP_PORT` in `.env` when needed.

Run in background:

```bash
docker compose -f docker/compose.yaml up --build -d
```

Stop containers:

```bash
docker compose -f docker/compose.yaml down
```
