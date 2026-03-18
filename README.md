# Ragib URL Shortener

A full-stack Spring Boot URL shortener with user accounts, private links, click tracking, and Docker support.

## What It Does
- Shortens long URLs into compact shareable links
- Supports guest link creation
- Lets signed-in users create private links and set expiry dates
- Tracks click counts
- Includes an admin dashboard for managing all links

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

## Docker

```bash
cd spring-boot-url-shortener
docker compose -f docker/compose.yaml up --build
```

Open `http://localhost:8080`

Run in background:

```bash
docker compose -f docker/compose.yaml up --build -d
```

Stop containers:

```bash
docker compose -f docker/compose.yaml down
```
