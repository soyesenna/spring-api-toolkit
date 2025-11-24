# Spring API Toolkit

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.0.1-blue.svg)](https://central.sonatype.com/)

> Standardized API response structure and pagination library for Spring Boot projects

English | [🇰🇷 한국어](./README.md)

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Installation](#installation)
  - [Quick Start](#quick-start)
- [API Response Structure](#api-response-structure)
  - [Basic Usage](#basic-usage)
  - [Response Format](#response-format)
  - [Advanced Usage](#advanced-usage)
- [Pagination](#pagination)
  - [Request Handling](#request-handling)
  - [Response Generation](#response-generation)
  - [Sorting](#sorting)
- [Configuration](#configuration)
- [Real-World Examples](#real-world-examples)
- [FAQ](#faq)
- [Contributing](#contributing)
- [License](#license)

## Features

### ✨ Standardized API Responses
- Consistent success/failure response structure
- Automatic HTTP status code and header handling
- Automatic validation error transformation
- Type-safe generic support

### 📄 Powerful Pagination
- Seamless Spring Data integration
- Frontend-friendly 1-based page numbering
- Type-safe sorting support
- Configurable default values

### 🚀 Ready to Use
- Auto-configuration with minimal setup
- Automatic RestControllerAdvice application
- Full Spring Boot 3.x compatibility

## Getting Started

### Installation

#### Gradle
```gradle
dependencies {
    implementation 'com.soyesenna:spring-api-toolkit:0.0.1'
}
```

#### Maven
```xml
<dependency>
    <groupId>com.soyesenna</groupId>
    <artifactId>spring-api-toolkit</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Quick Start

The library auto-activates when added to your project. No additional configuration needed.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ApiData<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        return ApiData.ok(user);
    }

    @GetMapping
    public ApiData<PagingResponse<UserDto>> getUsers(PagingRequest pagingRequest) {
        Pageable pageable = pagingRequest.toPageable();
        Page<User> page = userRepository.findAll(pageable);
        return ApiData.ok(PagingResponse.from(page));
    }
}
```

## API Response Structure

### Basic Usage

#### Success Responses
```java
// 200 OK
@GetMapping("/{id}")
public ApiData<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ApiData.ok(user);
}

// 201 CREATED
@PostMapping
public ApiData<User> createUser(@RequestBody CreateUserRequest request) {
    User user = userService.create(request);
    return ApiData.created(user);
}

// 204 NO CONTENT
@DeleteMapping("/{id}")
public ApiData<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ApiData.noContent();
}

// Custom HTTP Status
@GetMapping("/status")
public ApiData<String> customStatus() {
    return ApiData.from(HttpStatus.ACCEPTED, "Processing...");
}
```

#### Error Responses
```java
@GetMapping("/{id}")
public ApiData<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        return ApiData.error(
            HttpStatus.NOT_FOUND,
            404,
            "User not found"
        );
    }
    return ApiData.ok(user);
}
```

#### Validation Errors (Automatic Handling)
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiData<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {
        return ApiData.validationErrors(ex.getBindingResult().getFieldErrors());
    }
}
```

### Response Format

#### Success Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "code": 0,
  "message": "Request successful"
}
```

#### Error Response
```json
{
  "success": false,
  "data": null,
  "code": 404,
  "message": "User not found"
}
```

#### Validation Error Response
```json
{
  "success": false,
  "data": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  },
  "code": 400,
  "message": "Validation failed"
}
```

### Advanced Usage

#### Builder Pattern
```java
@GetMapping("/download")
public ApiData<byte[]> downloadFile() {
    byte[] fileData = fileService.getFile("document.pdf");

    return ApiData.<byte[]>builder()
        .httpStatus(HttpStatus.OK)
        .data(fileData)
        .contentType(MediaType.APPLICATION_PDF)
        .header("Content-Disposition", "attachment; filename=document.pdf")
        .code(0)
        .message("File download successful")
        .build();
}
```

#### Custom Headers
```java
@GetMapping("/data")
public ApiData<String> getDataWithHeaders() {
    return ApiData.<String>builder()
        .data("some data")
        .header("X-Custom-Header", "custom-value")
        .header("X-Request-Id", UUID.randomUUID().toString())
        .build();
}
```

#### ResponseEntity Conversion
```java
@GetMapping("/entity")
public ResponseEntity<Object> getAsResponseEntity() {
    ApiData<String> apiData = ApiData.ok("data");
    return apiData.toResponseEntity();
}
```

## Pagination

### Request Handling

#### Basic Usage
```java
@GetMapping("/users")
public ApiData<PagingResponse<UserDto>> getUsers(PagingRequest pagingRequest) {
    // Convert PagingRequest to Spring Data Pageable
    Pageable pageable = pagingRequest.toPageable();

    // Retrieve page from repository
    Page<User> page = userRepository.findAll(pageable);

    // Convert Page to PagingResponse
    return ApiData.ok(PagingResponse.from(page));
}
```

#### Request Parameters
```
GET /api/users?page=1&size=20
GET /api/users?page=2&size=10&sorts[0].property=name&sorts[0].direction=ASC
GET /api/users?page=1&size=20&sorts[0].property=createdAt&sorts[0].direction=DESC
```

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `page` | Integer | Page number (starts from 1) | 1 |
| `size` | Integer | Page size | 20 |
| `sorts` | Array | Sort criteria array | [] |

### Response Generation

#### Response Structure
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false,
    "sort": [
      {
        "property": "createdAt",
        "direction": "DESC"
      }
    ]
  },
  "code": 0,
  "message": "Request successful"
}
```

#### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `content` | Array | List of data for the current page |
| `page` | Integer | Current page number (starts from 1) |
| `size` | Integer | Page size |
| `totalElements` | Long | Total number of elements |
| `totalPages` | Integer | Total number of pages |
| `first` | Boolean | Whether this is the first page |
| `last` | Boolean | Whether this is the last page |
| `hasNext` | Boolean | Whether next page exists |
| `hasPrevious` | Boolean | Whether previous page exists |
| `sort` | Array | Applied sort criteria |

### Sorting

#### Single Sort
```java
// Frontend request
// GET /api/users?page=1&size=20&sorts[0].property=name&sorts[0].direction=ASC

@GetMapping("/users")
public ApiData<PagingResponse<UserDto>> getUsers(PagingRequest pagingRequest) {
    Pageable pageable = pagingRequest.toPageable();
    Page<User> page = userRepository.findAll(pageable);
    return ApiData.ok(PagingResponse.from(page));
}
```

#### Multiple Sorts
```java
// Frontend request
// GET /api/users?page=1&size=20
//   &sorts[0].property=status&sorts[0].direction=ASC
//   &sorts[1].property=createdAt&sorts[1].direction=DESC

// Automatically handled on backend - no additional code needed
```

#### Programmatic Sorting
```java
// Using static factory methods
SortRequest ascSort = SortRequest.asc("name");
SortRequest descSort = SortRequest.desc("createdAt");

// Creating custom PagingRequest
PagingRequest customRequest = new PagingRequest(
    1,
    20,
    List.of(
        SortRequest.asc("status"),
        SortRequest.desc("createdAt")
    )
);
```

## Configuration

### application.properties
```properties
# Pagination default values
api.page.default-page=1
api.page.default-size=20
```

### application.yml
```yaml
api:
  page:
    default-page: 1
    default-size: 20
```

> **Note**: If omitted, default values (page=1, size=20) are automatically used.

## Real-World Examples

### Search + Pagination
```java
@GetMapping("/search")
public ApiData<PagingResponse<UserDto>> searchUsers(
        @RequestParam String keyword,
        PagingRequest pagingRequest) {

    Pageable pageable = pagingRequest.toPageable();
    Page<User> page = userRepository.findByNameContaining(keyword, pageable);

    return ApiData.ok(PagingResponse.from(page));
}
```

### Conditional Sorting
```java
@GetMapping("/users")
public ApiData<PagingResponse<UserDto>> getUsers(
        @RequestParam(required = false) String sortBy,
        PagingRequest pagingRequest) {

    // Add custom sort if no default sort provided
    List<SortRequest> sorts = pagingRequest.sorts().isEmpty() && sortBy != null
        ? List.of(SortRequest.desc(sortBy))
        : pagingRequest.sorts();

    PagingRequest customRequest = new PagingRequest(
        pagingRequest.page(),
        pagingRequest.size(),
        sorts
    );

    Pageable pageable = customRequest.toPageable();
    Page<User> page = userRepository.findAll(pageable);

    return ApiData.ok(PagingResponse.from(page));
}
```

### Unified Error Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ApiData<Void> handleNotFound(EntityNotFoundException ex) {
        return ApiData.error(HttpStatus.NOT_FOUND, 404, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiData<Void> handleBadRequest(IllegalArgumentException ex) {
        return ApiData.error(HttpStatus.BAD_REQUEST, 400, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiData<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        return ApiData.validationErrors(ex.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(Exception.class)
    public ApiData<Void> handleGeneral(Exception ex) {
        return ApiData.error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            500,
            "Internal server error"
        );
    }
}
```

### DTO Transformation
```java
@GetMapping("/users")
public ApiData<PagingResponse<UserDto>> getUsers(PagingRequest pagingRequest) {
    Pageable pageable = pagingRequest.toPageable();
    Page<User> userPage = userRepository.findAll(pageable);

    // Transform Entity to DTO
    Page<UserDto> dtoPage = userPage.map(user -> new UserDto(
        user.getId(),
        user.getName(),
        user.getEmail()
    ));

    return ApiData.ok(PagingResponse.from(dtoPage));
}
```

## FAQ

### Q: Will endpoints not using ApiData be affected?
A: No. `ApiDataAdvice` only processes responses of type `ApiData`. Other response types remain unchanged.

### Q: I'm used to 0-based page numbering. Why 1-based?
A: `PagingRequest` uses frontend-friendly 1-based indexing, but internally converts to Spring Data's 0-based `Pageable` automatically (via `toPageable()` method, which does `page - 1`).

### Q: Are validation errors handled automatically?
A: The `ApiData.validationErrors()` method automatically transforms Spring's `FieldError` list into a Map format. Process it in your `@RestControllerAdvice`.

### Q: How do I handle file downloads?
A: Use the Builder pattern to set `contentType` and add headers. (See [Advanced Usage](#advanced-usage))

### Q: Can I set different default page sizes per project?
A: Yes, configure `api.page.default-size` in `application.properties` or `application.yml`. (See [Configuration](#configuration))

### Q: Can I use multiple sort criteria?
A: Yes, add multiple `SortRequest` objects to the `sorts` array. They're applied in array order. (See [Multiple Sorts](#multiple-sorts))

## Contributing

Bug reports, feature suggestions, and Pull Requests are always welcome!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

## Author

**Jooyoung Kim (soyesenna)**
- GitHub: [@soyesenna](https://github.com/soyesenna)
- Email: kjy915875@gmail.com

## Links

- [GitHub Repository](https://github.com/soyesenna/spring-api-toolkit)
- [Maven Central](https://central.sonatype.com/)
- [Issue Tracker](https://github.com/soyesenna/spring-api-toolkit/issues)

---

⭐️ Star this project if you find it helpful!
