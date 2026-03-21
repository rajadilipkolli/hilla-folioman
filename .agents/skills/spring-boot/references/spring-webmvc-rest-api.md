# Spring WebMVC REST APIs

- [Key principles](#key-principles)
- [Converter for PathVariable/RequestParam](#converter-for-pathvariablerequestparam-binding)
- [Value objects in request body](#binding-primitives-to-request-bodies-with-value-objects)
- [Global Exception Handler](#global-exception-handler)
- [Error response examples](#error-response-examples)

## Key principles
Follow these principles when creating REST APIs with Spring Web MVC:

- For Spring Boot 4.x projects, use Jackson 3.x library instead of Jackson 2.x 
- Use `tools.jackson.databind.json.JsonMapper` instead of `com.fasterxml.jackson.databind.ObjectMapper`
- Use **converters** to bind `@PathVariable` and `@RequestParam` to Value Objects
- Use **Jackson** for `@RequestBody` binding to Request Objects with Value Object properties
- Validate with `@Valid` annotation
- Return appropriate HTTP status codes
- Delegate to services for business logic execution
- Implement Global Exception Handler using `@RestControllerAdvice` and return `ProblemDetails` type response

### Converter for PathVariable/RequestParam Binding
```java
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserIdConverter implements Converter<String, UserId> {

    @Override
    public UserId convert(String source) {
        return new UserId(source);
    }
}
```

This allows Spring MVC to automatically convert path variables like `/{userId}` from String to `UserId`:

```java
@GetMapping("/{userId}")
ResponseEntity<UserVM> findUserById(@PathVariable UserId userId) {
    // userId is already an UserId object, not a String
}
```

### Binding primitives to Request Bodies with Value Objects
Use `@JsonValue` and `@JsonCreator` annotations to bind primitives to Request Bodies with Value Objects.

**UserId Value Object:**

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;\n\npublic record UserId(\n        @JsonValue \n        @NotBlank(message = \"User id cannot be null or empty\")\n        String id\n) {\n    @JsonCreator\n    public UserId {\n        if (id == null || id.trim().isEmpty()) {\n            throw new IllegalArgumentException(\"User id cannot be null\");\n        }\n    }\n\n    public static UserId of(String id) {\n        return new UserId(id);\n    }\n}\n```\n\n**CreateUserRequest Request Payload:**\n\n```java\nrecord CreateUserRequest(\n        @Valid UserId userId\n        // ... other properties\n) {\n}\n```\n\nSpring MVC will automatically bind the `userId` property from the JSON payload to `UserId` object.\n\n```json\n{\n  \"userId\": \"ABSHDJFSD\",\n  \"property-1\": \"value-1\",\n  \"property-n\": \"value-n\"\n}\n```\n\n### Global Exception Handler\nCreate a centralized exception handler that returns **ProblemDetail** responses.\n\nCreate a class `GlobalExceptionHandler` by following the following key principles:\n\n- Use `@RestControllerAdvice`\n- Extend `ResponseEntityExceptionHandler`\n- Return `ProblemDetail` for RFC 7807 compliance\n- Map different exceptions to appropriate HTTP status codes\n- Include validation errors in response\n- Hide internal details in production\n\n```java\nimport dev.sivalabs.onepoint.shared.DomainException;\nimport dev.sivalabs.onepoint.shared.ResourceNotFoundException;\nimport org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;\nimport org.springframework.context.support.DefaultMessageSourceResolvable;\nimport org.springframework.core.env.Environment;\nimport org.springframework.http.*;\nimport org.springframework.web.bind.MethodArgumentNotValidException;\nimport org.springframework.web.bind.annotation.ExceptionHandler;\nimport org.springframework.web.bind.annotation.RestControllerAdvice;\nimport org.springframework.web.context.request.WebRequest;\nimport org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;\n\nimport java.time.Instant;\nimport java.util.Arrays;\nimport java.util.List;\n\nimport static org.springframework.http.HttpStatus.NOT_FOUND;\nimport static org.springframework.http.HttpStatus.BAD_REQUEST;\nimport static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;\n\n@RestControllerAdvice\nclass GlobalExceptionHandler extends ResponseEntityExceptionHandler {\n    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);\n    private final Environment environment;\n\n    GlobalExceptionHandler(Environment environment) {\n        this.environment = environment;\n    }\n\n    @Override\n    public ResponseEntity<Object> handleMethodArgumentNotValid(\n            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {\n        log.error(\"Validation error\", ex);\n        var errors = ex.getAllErrors().stream()\n                .map(DefaultMessageSourceResolvable::getDefaultMessage)\n                .toList();\n\n        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());\n        problemDetail.setTitle(\"Validation Error\");\n        problemDetail.setProperty(\"errors\", errors);\n        problemDetail.setProperty(\"timestamp\", Instant.now());\n        return ResponseEntity.status(UNPROCESSABLE_CONTENT).body(problemDetail);\n    }\n\n    @ExceptionHandler(DomainException.class)\n    public ProblemDetail handle(DomainException e) {\n        log.warn(\"Bad request\", e);\n        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, e.getMessage());\n        problemDetail.setTitle(\"Bad Request\");\n        problemDetail.setProperty(\"errors\", List.of(e.getMessage()));\n        problemDetail.setProperty(\"timestamp\", Instant.now());\n        return problemDetail;\n    }\n\n    @ExceptionHandler(ResourceNotFoundException.class)\n    public ProblemDetail handle(ResourceNotFoundException e) {\n        log.error(\"Resource not found\", e);\n        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND, e.getMessage());\n        problemDetail.setTitle(\"Resource Not Found\");\n        problemDetail.setProperty(\"errors\", List.of(e.getMessage()));\n        problemDetail.setProperty(\"timestamp\", Instant.now());\n        return problemDetail;\n    }\n\n    @ExceptionHandler(Exception.class)\n    ProblemDetail handleUnexpected(Exception e) {\n        log.error(\"Unexpected exception occurred\", e);\n\n        // Don't expose internal details in production\n        String message = \"An unexpected error occurred\";\n        if (isDevelopmentMode()) {\n            message = e.getMessage();\n        }\n\n        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(INTERNAL_SERVER_ERROR, message);\n        problemDetail.setProperty(\"timestamp\", Instant.now());\n        return problemDetail;\n    }\n\n    private boolean isDevelopmentMode() {\n        List<String> profiles = Arrays.asList(environment.getActiveProfiles());\n        return profiles.contains(\"dev\") || profiles.contains(\"local\");\n    }\n}\n```\n\n#### Error Response Examples\n\n**Validation Error (400):**\n```json\n{\n  \"type\": \"about:blank\",\n  \"title\": \"Validation Error\",\n  \"status\": 400,\n  \"detail\": \"Validation failed for argument...\",\n  \"errors\": [\n    \"Title is required\",\n    \"Email must be valid\"\n  ]\n}\n```\n\n**Domain Exception (400):**\n```json\n{\n  \"type\": \"about:blank\",\n  \"title\": \"Bad Request\",\n  \"status\": 400,\n  \"detail\": \"Cannot update user details\",\n  \"errors\": [\n    \"Email is already exist\"\n  ]\n}\n```\n\n**Resource Not Found (404):**\n```json\n{\n  \"type\": \"about:blank\",\n  \"title\": \"Resource Not Found\",\n  \"status\": 404,\n  \"detail\": \"User not found with id: ABC123\",\n  \"errors\": [\n    \"User not found with id: ABC123\"\n  ]\n}\n```\n\n**Internal Server Error (500):**\n```json\n{\n  \"type\": \"about:blank\",\n  \"title\": \"Internal Server Error\",\n  \"status\": 500,\n  \"detail\": \"An unexpected error occurred\",\n  \"timestamp\": \"2024-01-15T10:30:00Z\"\n}\n```
