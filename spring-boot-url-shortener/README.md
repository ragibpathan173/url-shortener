# Ragib URL Shortener

Ragib URL Shortener is a Spring Boot web application for creating short, shareable links from long URLs.

## Features
- Create short URLs from long links
- Optional expiration for links
- Public and private short URLs
- User registration and login
- Admin dashboard
- Click tracking

## Tech Stack
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- H2 Database
- Docker Compose

## Run Locally

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The app will start at:

```text
http://localhost:8080
```

## Docker

Start PostgreSQL only:

```bash
docker compose up -d
```

Or use the app container setup inside the `docker` folder:

```bash
cd docker
docker compose up -d
```

Build the app image locally:

```bash
docker build -t ragib-url-shortener .
```

Run the app container with the bundled H2 database:

```bash
docker run --rm -p 8080:8080 ragib-url-shortener
```

## Project Structure

- `src/main/java` - backend source code
- `src/main/resources/templates` - Thymeleaf frontend templates
- `src/main/resources/static/css` - stylesheets
- `src/main/resources/db/migration` - SQL migration files
- `src/test/java` - test code
