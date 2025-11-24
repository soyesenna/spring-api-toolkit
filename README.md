# Spring API Toolkit

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.0.1-blue.svg)](https://central.sonatype.com/)

> Spring Boot 프로젝트를 위한 표준화된 API 응답, 페이지네이션, 타입 세이프 예외 처리 라이브러리

[🌐 English](./README.en.md) | 한국어

## 목차

- [주요 기능](#주요-기능)
- [시작하기](#시작하기)
  - [설치](#설치)
  - [빠른 시작](#빠른-시작)
- [API 응답 구조](#api-응답-구조)
  - [기본 사용법](#기본-사용법)
  - [응답 형식](#응답-형식)
  - [고급 사용법](#고급-사용법)
- [페이지네이션](#페이지네이션)
  - [요청 처리](#요청-처리)
  - [응답 생성](#응답-생성)
  - [정렬 기능](#정렬-기능)
- [예외 처리](#예외-처리)
- [설정](#설정)
- [실무 예제](#실무-예제)
- [FAQ](#faq)
- [기여하기](#기여하기)
- [라이선스](#라이선스)

## 주요 기능

### ✨ 표준화된 API 응답
- 일관된 성공/실패 응답 구조
- 자동 HTTP 상태 코드 및 헤더 처리
- Validation 에러 자동 변환
- 타입 안전한 제네릭 지원
- 타입 안전한 에러 코드 및 전역 예외 처리
- Swagger에 에러 응답 예제 자동 등록

### 📄 강력한 페이지네이션
- Spring Data와 완벽 통합
- 프론트엔드 친화적인 1-based 페이지 번호
- 타입 안전한 정렬 지원
- 설정 가능한 기본값

### 🚀 즉시 사용 가능
- 최소 설정으로 자동 활성화
- RestControllerAdvice 자동 적용
- Spring Boot 3.x 완벽 호환

## 시작하기

### 설치

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

### 빠른 시작

라이브러리를 추가하면 자동으로 활성화됩니다. 추가 설정 없이 바로 사용할 수 있습니다.

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

## API 응답 구조

### 기본 사용법

#### 성공 응답
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

// 커스텀 HTTP 상태
@GetMapping("/status")
public ApiData<String> customStatus() {
    return ApiData.from(HttpStatus.ACCEPTED, "처리 중입니다.");
}
```

#### 에러 응답
```java
@GetMapping("/{id}")
public ApiData<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        return ApiData.error(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "사용자를 찾을 수 없습니다."
        );
    }
    return ApiData.ok(user);
}
```

#### Validation 에러 (자동 처리)
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

### 응답 형식

#### 성공 응답
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com"
  },
  "code": "COMMON-00000",
  "message": "요청에 성공했습니다."
}
```

#### 에러 응답
```json
{
  "success": false,
  "data": {
    "path": "/api/users/1",
    "type": "UserErrorCode",
    "timestamp": "2025-01-24T10:15:30Z",
    "logLevel": "ERROR"
  },
  "code": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다."  // MessageFormat으로 파라미터 대체 가능
}
```

#### Validation 에러
```json
{
  "success": false,
  "data": {
    "email": "이메일 형식이 올바르지 않습니다.",
    "password": "비밀번호는 8자 이상이어야 합니다."
  },
  "code": "COMMON-00001",
  "message": "요청 유효성 검증에 실패했습니다."
}
```

### 고급 사용법

#### Builder 패턴
```java
@GetMapping("/download")
public ApiData<byte[]> downloadFile() {
    byte[] fileData = fileService.getFile("document.pdf");

    return ApiData.<byte[]>builder()
        .httpStatus(HttpStatus.OK)
        .data(fileData)
        .contentType(MediaType.APPLICATION_PDF)
        .header("Content-Disposition", "attachment; filename=document.pdf")
        .code("FILE_DOWNLOAD_SUCCESS")
        .message("파일 다운로드 성공")
        .build();
}
```

#### 커스텀 헤더
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

#### ResponseEntity 변환
```java
@GetMapping("/entity")
public ResponseEntity<Object> getAsResponseEntity() {
    ApiData<String> apiData = ApiData.ok("data");
    return apiData.toResponseEntity();
}
```

## 페이지네이션

### 요청 처리

#### 기본 사용
```java
@GetMapping("/users")
public ApiData<PagingResponse<UserDto>> getUsers(PagingRequest pagingRequest) {
    // PagingRequest를 Spring Data Pageable로 변환
    Pageable pageable = pagingRequest.toPageable();

    // Repository에서 페이지 조회
    Page<User> page = userRepository.findAll(pageable);

    // Page를 PagingResponse로 변환하여 반환
    return ApiData.ok(PagingResponse.from(page));
}
```

#### 요청 파라미터
```
GET /api/users?page=1&size=20
GET /api/users?page=2&size=10&sorts[0].property=name&sorts[0].direction=ASC
GET /api/users?page=1&size=20&sorts[0].property=createdAt&sorts[0].direction=DESC
```

| 파라미터 | 타입 | 설명 | 기본값 |
|---------|------|------|--------|
| `page` | Integer | 페이지 번호 (1부터 시작) | 1 |
| `size` | Integer | 페이지 크기 | 20 |
| `sorts` | Array | 정렬 조건 배열 | [] |

### 응답 생성

#### 응답 구조
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "홍길동",
        "email": "hong@example.com"
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
  "code": "COMMON-00000",
  "message": "요청에 성공했습니다."
}
```

#### 필드 설명

| 필드 | 타입 | 설명 |
|-----|------|------|
| `content` | Array | 현재 페이지의 데이터 목록 |
| `page` | Integer | 현재 페이지 번호 (1부터 시작) |
| `size` | Integer | 페이지 크기 |
| `totalElements` | Long | 전체 요소 개수 |
| `totalPages` | Integer | 전체 페이지 개수 |
| `first` | Boolean | 첫 번째 페이지 여부 |
| `last` | Boolean | 마지막 페이지 여부 |
| `hasNext` | Boolean | 다음 페이지 존재 여부 |
| `hasPrevious` | Boolean | 이전 페이지 존재 여부 |
| `sort` | Array | 적용된 정렬 조건 |

### 정렬 기능

#### 단일 정렬
```java
// 프론트엔드에서 요청
// GET /api/users?page=1&size=20&sorts[0].property=name&sorts[0].direction=ASC

@GetMapping("/users")
public ApiData<PagingResponse<UserDto>> getUsers(PagingRequest pagingRequest) {
    Pageable pageable = pagingRequest.toPageable();
    Page<User> page = userRepository.findAll(pageable);
    return ApiData.ok(PagingResponse.from(page));
}
```

#### 다중 정렬
```java
// 프론트엔드에서 요청
// GET /api/users?page=1&size=20
//   &sorts[0].property=status&sorts[0].direction=ASC
//   &sorts[1].property=createdAt&sorts[1].direction=DESC

// 백엔드에서 자동 처리됨 - 추가 코드 불필요
```

#### 프로그래매틱 정렬
```java
// 정적 팩토리 메서드 사용
SortRequest ascSort = SortRequest.asc("name");
SortRequest descSort = SortRequest.desc("createdAt");

// 커스텀 PagingRequest 생성
PagingRequest customRequest = new PagingRequest(
    1,
    20,
    List.of(
        SortRequest.asc("status"),
        SortRequest.desc("createdAt")
    )
);
```

## 예외 처리

### ErrorCode 정의
```java
public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 {0}을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이메일 {0}이 이미 존재합니다.");

    private final HttpStatus status;
    private final String message;

    UserErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return "USER_" + this.name(); // 미구현 시 enum 이름으로 자동 채움
    }
}
```

### 예외 발생 & AssertToolkit
```java
@Service
public class UserService {

    public User findById(String id) {
        return userRepository.findById(id)
            .orElseThrow(UserErrorCode.USER_NOT_FOUND.args(id));
    }

    public void create(String email) {
        AssertToolkit.hasText(email, CommonErrorCode.INVALID_EMAIL, email);
    }
}
```
- `CommonErrorCode`는 프로젝트 공통 에러 코드를 담는 예시 enum입니다.

### 전역 예외 응답 (자동)
- `CoreException`을 던지면 `GlobalExceptionHandler`가 `ApiData`로 감싸 응답합니다.
- 메시지는 `MessageFormat`으로 파라미터를 치환하고, `getCode()`가 비어있으면 enum 이름을 사용합니다.
- 예상치 못한 예외는 `UNEXPECTED_ERROR` 코드와 함께 500으로 응답합니다.

```json
{
  "success": false,
  "data": {
    "path": "/api/users/123",
    "type": "UserErrorCode",
    "timestamp": "2025-01-24T10:15:30Z",
    "logLevel": "ERROR"
  },
  "code": "USER_NOT_FOUND",
  "message": "사용자 123을 찾을 수 없습니다."
}
```

### Swagger 문서화
```java
@ApiErrorCode({UserErrorCode.class, AuthErrorCode.class})
@GetMapping("/{id}")
public User getUser(@PathVariable String id) {
    return userService.findById(id);
}
```
- `ApiErrorCodeOperationCustomizer`가 에러 코드별 `ApiData` 예제를 Swagger에 자동 추가합니다.

## 설정

### application.properties
```properties
# 페이지네이션 기본값 설정
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

> **참고**: 설정을 생략하면 기본값(page=1, size=20)이 자동으로 사용됩니다.

## 실무 예제

### 검색 + 페이지네이션
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

### 조건부 정렬
```java
@GetMapping("/users")
public ApiData<PagingResponse<UserDto>> getUsers(
        @RequestParam(required = false) String sortBy,
        PagingRequest pagingRequest) {

    // 기본 정렬 조건이 없는 경우 커스텀 정렬 추가
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

### 에러 처리 통합
```java
@RestControllerAdvice
public class CustomExceptionBridge {

    @ExceptionHandler(EntityNotFoundException.class)
    public CoreException handleNotFound(EntityNotFoundException ex) {
        return UserErrorCode.USER_NOT_FOUND.throwException();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CoreException handleBadRequest(IllegalArgumentException ex) {
        return CommonErrorCode.INVALID_REQUEST.throwException();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiData<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        return ApiData.validationErrors(ex.getBindingResult().getFieldErrors());
    }
}
```

### DTO 변환
```java
@GetMapping("/users")
public ApiData<PagingResponse<UserDto>> getUsers(PagingRequest pagingRequest) {
    Pageable pageable = pagingRequest.toPageable();
    Page<User> userPage = userRepository.findAll(pageable);

    // Entity를 DTO로 변환
    Page<UserDto> dtoPage = userPage.map(user -> new UserDto(
        user.getId(),
        user.getName(),
        user.getEmail()
    ));

    return ApiData.ok(PagingResponse.from(dtoPage));
}
```

## FAQ

### Q: ApiData를 사용하지 않는 엔드포인트도 영향을 받나요?
A: 아니요. `ApiDataAdvice`는 `ApiData` 타입의 응답만 처리합니다. 다른 타입의 응답은 그대로 유지됩니다.

### Q: 페이지 번호가 0부터 시작하는 게 익숙한데요?
A: `PagingRequest`는 프론트엔드 친화적인 1-based 인덱싱을 사용하지만, 내부적으로 Spring Data의 0-based `Pageable`로 자동 변환됩니다. (`toPageable()` 메서드에서 `page - 1` 처리)

### Q: Validation 에러가 자동으로 처리되나요?
A: `ApiData.validationErrors()` 메서드를 사용하면 Spring의 `FieldError` 리스트를 자동으로 Map 형태로 변환해줍니다. `@RestControllerAdvice`에서 처리하면 됩니다.

### Q: 파일 다운로드는 어떻게 처리하나요?
A: Builder 패턴을 사용하여 `contentType`을 설정하고 헤더를 추가하면 됩니다. ([고급 사용법](#고급-사용법) 참조)

### Q: 기본 페이지 크기를 프로젝트별로 다르게 설정할 수 있나요?
A: 네, `application.properties` 또는 `application.yml`에서 `api.page.default-size`를 설정하면 됩니다. ([설정](#설정) 참조)

### Q: 정렬 조건을 여러 개 사용할 수 있나요?
A: 네, `sorts` 배열에 여러 `SortRequest`를 추가하면 됩니다. 배열 순서대로 정렬이 적용됩니다. ([다중 정렬](#다중-정렬) 참조)

## 기여하기

버그 리포트, 기능 제안, Pull Request는 언제나 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 라이선스

이 프로젝트는 Apache License 2.0 라이선스를 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 개발자

**Jooyoung Kim (soyesenna)**
- GitHub: [@soyesenna](https://github.com/soyesenna)
- Email: kjy915875@gmail.com

## 링크

- [GitHub Repository](https://github.com/soyesenna/spring-api-toolkit)
- [Maven Central](https://central.sonatype.com/)
- [Issue Tracker](https://github.com/soyesenna/spring-api-toolkit/issues)

---

⭐️ 이 프로젝트가 도움이 되셨다면 Star를 눌러주세요!
