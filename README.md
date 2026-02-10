# Task Management API

Mid-level Spring Boot REST API demonstrating JWT Authentication, Pagination, Caching, and AOP.

> **Level:** Mid-level (2-5 years)
> **Concepts:** JWT, Pagination, Caching, AOP, API Versioning, DTO Pattern

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Spring Boot 3.2.5, Java 17 | Application framework |
| H2 Database (in-memory) | Persistence |
| Spring Security + JWT (jjwt 0.12.5) | Stateless authentication |
| Caffeine Cache | In-memory caching |
| Spring AOP | Cross-cutting concerns |
| JUnit 5 + MockMvc + Mockito | Testing |

## Domain Model

```
User (auth)
  └── has many → Project
                   └── has many → Task
                                    ├── status: TODO/IN_PROGRESS/DONE
                                    ├── priority: LOW/MEDIUM/HIGH
                                    └── assignee → User
```

## Quick Start

```bash
# Compile
mvn compile

# Run tests (19 tests)
mvn test

# Run application
mvn spring-boot:run
```

The app starts at `http://localhost:8080` with H2 console at `/h2-console`.

Seed data creates 3 users (`admin`, `john`, `jane`) with password: `password123`

## API Endpoints

### Authentication (no auth required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT token |

### Projects (JWT required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/projects` | List all projects |
| GET | `/api/v1/projects/{id}` | Get project (cached) |
| POST | `/api/v1/projects` | Create project |
| PUT | `/api/v1/projects/{id}` | Update project |
| DELETE | `/api/v1/projects/{id}` | Delete project (ADMIN only) |

### Tasks (JWT required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/tasks?page=0&size=10&sort=createdAt,desc` | List tasks (paginated) |
| GET | `/api/v1/tasks/{id}` | Get task |
| GET | `/api/v1/tasks/project/{projectId}` | Tasks by project |
| GET | `/api/v1/tasks/assignee/{assigneeId}` | Tasks by assignee |
| POST | `/api/v1/tasks` | Create task |
| PUT | `/api/v1/tasks/{id}` | Update task |
| DELETE | `/api/v1/tasks/{id}` | Delete task |

### Example Usage

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"password123","email":"new@demo.com"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123"}' | jq -r '.token')

# Create project
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Project","description":"A new project"}'

# List tasks with pagination
curl http://localhost:8080/api/v1/tasks?page=0&size=5&sort=priority,desc \
  -H "Authorization: Bearer $TOKEN"
```

## Project Structure

```
com.demo.taskapi/
├── config/           SecurityConfig, CacheConfig, AppProperties
├── security/         JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
├── controller/       AuthController, ProjectController, TaskController
├── service/          AuthService, ProjectService, TaskService
├── repository/       UserRepository, ProjectRepository, TaskRepository
├── entity/           User, Project, Task + Enums (Role, TaskStatus, TaskPriority)
├── dto/              request/response DTOs + PagedResponse
├── exception/        GlobalExceptionHandler + custom exceptions
├── aspect/           LoggingAspect (AOP)
└── TaskApiApplication.java
```

## Mid-Level Concepts Demonstrated

1. **JWT Authentication** -- JwtTokenProvider, JwtAuthFilter, SecurityConfig
2. **Pagination & Sorting** -- Pageable, PagedResponse, @PageableDefault
3. **Caching** -- @Cacheable, @CacheEvict, Caffeine CacheManager
4. **AOP** -- @Aspect, @Around, LoggingAspect
5. **API Versioning** -- URI-based `/api/v1/`
6. **DTO Pattern** -- Separate Create/Update/Response DTOs
7. **Role-Based Access** -- @PreAuthorize, @EnableMethodSecurity
