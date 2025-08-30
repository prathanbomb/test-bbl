# Test BBL - Spring WebFlux User Management API

A reactive Spring Boot application for user management with standardized API responses and comprehensive validation.

## Features

- **Reactive Programming**: Built with Spring WebFlux for non-blocking I/O
- **Global API Response**: Standardized response format across all endpoints
- **Separate DTOs**: Distinct request/response models for security and clarity
- **Validation**: Comprehensive input validation with detailed error responses
- **Database**: R2DBC with H2 (dev) and PostgreSQL (prod) support
- **Testing**: Complete test coverage for controllers and services

## Tech Stack

- **Framework**: Spring Boot 3.5.4
- **Reactive**: Spring WebFlux + R2DBC
- **Database**: H2 (dev), PostgreSQL (prod)
- **Migration**: Flyway
- **Mapping**: MapStruct
- **Validation**: Jakarta Bean Validation
- **Testing**: JUnit 5, MockitoExtension, WebTestClient

## API Endpoints

### Users API

All responses follow the standardized `ApiResponse<T>` format:

```json
{
  "timestamp": "2025-08-30T16:00:00Z",
  "status": 200,
  "message": "Success",
  "data": {...},
  "pagination": {...}
}
```

#### GET /users
Get paginated list of users
- **Query Params**: `page` (default: 0), `size` (default: 10)
- **Response**: `ApiResponse<List<UserResponse>>` with pagination info

#### GET /users/{id}
Get user by ID
- **Response**: `ApiResponse<UserResponse>`
- **Errors**: 404 if user not found

#### POST /users
Create new user
- **Body**: `CreateUserRequest` (name, username, email required)
- **Response**: `ApiResponse<UserResponse>` (201 Created)
- **Errors**: 409 if email exists, 400 for validation

#### PUT /users/{id}
Update existing user
- **Body**: `UpdateUserRequest` (all fields optional)
- **Response**: `ApiResponse<UserResponse>`
- **Errors**: 404 if not found, 409 if email conflict

#### DELETE /users/{id}
Delete user
- **Response**: `ApiResponse<Void>` (204 No Content)
- **Errors**: 404 if user not found

## Request/Response Models

### CreateUserRequest
```json
{
  "name": "John Doe",
  "username": "johndoe", 
  "email": "john@example.com",
  "phone": "+1234567890",
  "website": "https://johndoe.com"
}
```

### UpdateUserRequest
```json
{
  "name": "Updated Name",
  "email": "newemail@example.com"
}
```

### UserResponse
```json
{
  "id": 1,
  "name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com", 
  "phone": "+1234567890",
  "website": "https://johndoe.com"
}
```

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Development
```bash
./mvnw spring-boot:run
```

### Production with Docker
```bash
docker-compose up --build
```

### Testing
```bash
./mvnw test
```

## Configuration

### Environment Profiles
- **dev**: H2 in-memory database
- **prod**: PostgreSQL database

### Database Configuration
Configure in `application-{profile}.yml`:
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/testbbl
    username: ${DB_USERNAME:user}
    password: ${DB_PASSWORD:password}
```

## Project Structure

```
src/main/java/com/example/testbbl/
├── controller/           # REST controllers
├── dto/
│   ├── request/         # Request DTOs (CreateUserRequest, UpdateUserRequest)  
│   ├── response/        # Response DTOs (UserResponse)
│   ├── ApiResponse.java # Global response wrapper
│   └── PaginationInfo.java
├── service/             # Business logic
├── repository/          # Data access layer
├── model/              # JPA entities
├── mapper/             # MapStruct mappers
└── exception/          # Exception handling
```

## Error Handling

Global exception handler provides consistent error responses:

```json
{
  "timestamp": "2025-08-30T16:00:00Z",
  "status": 400,
  "message": "Validation failure",
  "errors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Email must be valid"
    }
  ]
}
```

## Development Notes

- Uses reactive streams throughout the stack
- MapStruct handles DTO/Entity mapping automatically
- Global exception handling ensures consistent error responses
- Comprehensive validation on request DTOs
- Pagination support for list endpoints