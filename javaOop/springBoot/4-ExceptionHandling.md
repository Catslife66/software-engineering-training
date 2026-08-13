# Exception Handling

Exceptions represent situations where the normal application flow cannot continue.

In BrightMove, examples include:

```
Property doesn't exist
Invalid request data
Viewing slot unavailable
Stale version during update
Malformed UUID
Unexpected database failure
```

Without a centralized strategy, controllers can become full of repetitive error handling:

```
try {
    ...
} catch (...) {
    ...
}
```

Spring gives us a cleaner architecture:

```
Controller
    ↓
Service
    ↓
Exception thrown
    ↓
@RestControllerAdvice
    ↓
Exception Handler
    ↓
Consistent HTTP error response
```

The goal of this module is to understand how exceptions travel through a Spring application and how we translate them into appropriate HTTP responses.

## Core Concepts

There are two different concepts we should separate:

```
Exception
    =
Something went wrong inside Java/application flow

HTTP error response
    =
How we communicate that failure to an API client
```

For example, our service might throw:

```
throw new PropertyNotFoundException(id);
```

That exception itself doesn't inherently mean HTTP 404.

The HTTP layer decides:

```
PropertyNotFoundException
        ↓
404 Not Found
```

This separation is important because the service should not need to understand HTTP.

**Exception propagation**

Consider:

```
public PropertyResponse getPropertyById(UUID id) {

    Property property = propertyRepository.findById(id)
            .orElseThrow(
                () -> new PropertyNotFoundException(id)
            );

    return propertyMapper.toResponse(property);
}
```

If the property exists:

```
findById()
    ↓
Property found
    ↓
map to response
    ↓
return normally
```

If it doesn't:

```
findById()
    ↓
Optional.empty()
    ↓
orElseThrow()
    ↓
PropertyNotFoundException
```

Execution of that method stops.

The exception propagates upward:

```
Repository
    ↓
Service throws
    ↓
Controller call cannot complete normally
    ↓
Spring MVC
    ↓
Exception handling mechanism
```

## Mental Model

Think of normal execution and exceptional execution as two different paths.

**Success path**

```
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
Service result
    ↓
Controller
    ↓
200 OK
```

**Failure path**

```
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Something fails
    ↓
Exception
    ──────────────────┐
                      ↓
              GlobalExceptionHandler
                      ↓
               HTTP error response
```

The exception effectively exits the normal path.

This is why controllers do not normally need:

```
try {
    propertyService.getPropertyById(id);
} catch (PropertyNotFoundException e) {
    ...
}
```

A global handler deals with it centrally.

## Spring / Java Implementation

**Custom Exceptions**

Suppose BrightMove needs to represent:

Property with this ID doesn't exist.

We can create:

```
public class PropertyNotFoundException
        extends RuntimeException {

    public PropertyNotFoundException(UUID id) {
        super("Property not found with id: " + id);
    }
}
```

Then:

```
Property property = propertyRepository.findById(id)
        .orElseThrow(
            () -> new PropertyNotFoundException(id)
        );
```

The service expresses the application failure clearly.

Compare:

```
throw new RuntimeException("Property not found");
```

with:

```
throw new PropertyNotFoundException(id);
```

The second version communicates the meaning of the failure through its type.

---

`@RestControllerAdvice`

We now need somewhere to translate exceptions into HTTP responses.

```
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

@RestControllerAdvice applies exception handling across REST controllers.

Instead of putting handlers inside every controller, we centralize them.

Conceptually:

```
PropertyController ───┐
ViewingController ────┼──→ GlobalExceptionHandler
AgentController ──────┘
```

---

`@ExceptionHandler`

Inside the advice:

```
@ExceptionHandler(PropertyNotFoundException.class)
public ResponseEntity<ErrorResponse> handlePropertyNotFound(
        PropertyNotFoundException exception
) {
    ...
}
```

This tells Spring:

```
If a controller request results in PropertyNotFoundException, use this method to build the HTTP response.
```

For example:

```
@ExceptionHandler(PropertyNotFoundException.class)
public ResponseEntity<ErrorResponse> handlePropertyNotFound(
        PropertyNotFoundException exception
) {

    ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            exception.getMessage()
    );

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response);
}
```

Now:

```
PropertyNotFoundException
        ↓
handler
        ↓
404 Not Found
```

---

**Error Response DTO**

Rather than returning arbitrary maps:

```
Map<String, Object>
```

a dedicated DTO usually gives us a cleaner API contract.

For example:

```
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}
```

A response could become:

```
{
  "timestamp": "2026-08-12T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Property not found with id: ..."
}
```

Every API error can follow the same structure.

That's what we mean by a consistent error contract.

---

**Validation Exceptions** - MethodArgumentNotValid

In Module 3 we had:

```
@Valid @RequestBody CreatePropertyRequest request
```

If validation fails, Spring commonly raises:

```
MethodArgumentNotValidException
```

We can handle that centrally too.

```
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ValidationErrorResponse> handleValidation(
        MethodArgumentNotValidException exception
) {
    ...
}
```

The exception contains information about which fields failed validation.

For example:

```
title → Title is required
price → Price must be greater than zero
```

We can extract those errors:

```
Map<String, String> errors = new HashMap<>();

exception.getBindingResult()
        .getFieldErrors()
        .forEach(error ->
                errors.put(
                    error.getField(),
                    error.getDefaultMessage()
                )
        );
```

Then return something such as:

```
{
  "status": 400,
  "error": "Bad Request",
  "fieldErrors": {
    "title": "Title is required",
    "price": "Price must be greater than zero"
  }
}
```

This gives frontend code useful information about exactly what needs correcting.

---

**Invalid Path Variables** - MethodArgumentTypeMismatch

Consider:

```
GET /properties/hello
```

but our controller expects:

```
@PathVariable UUID id
```

Spring cannot convert:

```
"hello"
```

into:

```
UUID
```

This failure occurs **before the controller method executes normally**.

Spring raises a type-conversion exception, commonly handled through:

```
MethodArgumentTypeMismatchException
```

We can provide a handler:

```
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception
) {

    ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Invalid value for '" +
                    exception.getName() + "'"
    );

    return ResponseEntity
            .badRequest()
            .body(response);
}
```

So:

```
/properties/not-a-uuid
        ↓
UUID conversion fails
        ↓
MethodArgumentTypeMismatchException
        ↓
400 Bad Request
```

Notice that @Valid is irrelevant here.

There is no valid UUID object to validate because conversion failed first.

---

**Malformed JSON** - HttpMessageNotReadable

Another boundary failure is malformed or unreadable request content.

For example:

```
{
  "price": "hello"
}
```

when price expects:

```
BigDecimal
```

Jackson cannot construct the DTO correctly.

This commonly results in:

```
HttpMessageNotReadableException
```

which can be mapped to:

```
400 Bad Request
```

Again, this occurs before normal Bean Validation.

The order is roughly:

```
Read HTTP request
    ↓
Deserialize / convert
    ↓
Bean Validation
    ↓
Controller method
```

---

**Business Exceptions**

Some errors originate in the service because they depend on business state.

For example:

```
if (property.getStatus() == PropertyStatus.SOLD) {
    throw new ViewingNotAllowedException(propertyId);
}
```

The service doesn't return:

```
ResponseEntity.status(...)
```

It simply expresses:

```
This business operation cannot continue.
```

The HTTP layer decides how that failure should be represented.

For example:

```
@ExceptionHandler(ViewingNotAllowedException.class)
public ResponseEntity<ErrorResponse> handleViewingNotAllowed(
        ViewingNotAllowedException exception
) {

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(...);
}
```

---

**Choosing HTTP Status Codes**

Exception handling isn't just about catching exceptions.

We need to map failure semantics correctly.

**400 Bad Request**

The request itself is invalid.

Examples:

```
Invalid UUID format
Malformed JSON
Bean Validation failure
```

**401 Unauthorized**

Despite the name, this generally means:

Authentication is missing or invalid.

Example:

```
No valid authentication credentials
```

We'll cover this properly in Spring Security.

**403 Forbidden**

The user is authenticated but isn't allowed to perform the operation.

Example:

```
Agent A attempts to modify Agent B's property.
```

**404 Not Found**

Requested resource doesn't exist.

```
PropertyNotFoundException
ViewingNotFoundException
```

**409 Conflict**

The request conflicts with the current state of the system.

Examples:

```
Viewing slot already booked

Optimistic locking conflict

Operation conflicts with current resource state
```

**500 Internal Server Error**

Unexpected server-side failure.

This is different from expected business exceptions.

For example:

```
NullPointerException
Unexpected infrastructure failure
Unexpected programming error
```

We should avoid exposing internal implementation details to clients.

---

**Checked vs Unchecked Exceptions**

Java has two broad exception categories.

Checked

Examples:

```
IOException
SQLException
```

The compiler forces us to catch or declare them.

Unchecked

Classes extending:

```
RuntimeException
```

are unchecked.

Our application exceptions commonly use:

```
public class PropertyNotFoundException
        extends RuntimeException {
}
```

Why?

Because failures such as:

```
Property doesn't exist
Viewing cannot be booked
Version conflict
```

aren't normally conditions the immediate caller can recover from by continuing the same workflow.

They propagate to the application boundary where they are translated appropriately.

## Request & Data Flow

Let's trace:

```
GET /properties/{id}
```

Controller:

```
@GetMapping("/{id}")
public PropertyResponse getProperty(
    @PathVariable UUID id
) {
    return propertyService.getPropertyById(id);
}
```

Service:

```
public PropertyResponse getPropertyById(UUID id) {

    Property property = propertyRepository.findById(id)
            .orElseThrow(
                () -> new PropertyNotFoundException(id)
            );

    return propertyMapper.toResponse(property);

}
```

Suppose the database doesn't contain the property.

Flow:

```
HTTP GET
    ↓
Spring converts path variable to UUID
    ↓
PropertyController
    ↓
PropertyService
    ↓
PropertyRepository.findById()
    ↓
Optional.empty()
    ↓
orElseThrow()
    ↓
PropertyNotFoundException
    ↓
normal controller flow stops
    ↓
GlobalExceptionHandler
    ↓
handlePropertyNotFound()
    ↓
ErrorResponse
    ↓
404 Not Found
```

This gives us clean separation:

```
Repository
"What data exists?"

Service
"What does missing data mean?"

Exception
"Describe the failure"

Global Handler
"How should HTTP represent the failure?"
```

## BrightMove Example

A simplified exception package might eventually contain:

```
exception/
├── PropertyNotFoundException.java
├── ViewingNotFoundException.java
├── ViewingNotAllowedException.java
└── GlobalExceptionHandler.java
```

Example exception:

```
public class PropertyNotFoundException
        extends RuntimeException {

    public PropertyNotFoundException(UUID id) {
        super("Property not found with id: " + id);
    }
}
```

Error DTO:

```
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}
```

Global Handler:

```
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePropertyNotFound(
            PropertyNotFoundException exception
    ) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid value for '" +
                        exception.getName() + "'"
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }
}
```

The controllers remain clean:

```
@GetMapping("/{id}")
public PropertyResponse getProperty(
        @PathVariable UUID id
) {
    return propertyService.getPropertyById(id);
}
```

There is no try/catch here.

## Common Mistakes

**Catching business exceptions in every controller**

Avoid:

```
try {
    return propertyService.getPropertyById(id);
} catch (PropertyNotFoundException e) {
    ...
}
```

Repeated across controllers.

Centralize HTTP exception translation with `@RestControllerAdvice`.

**Throwing generic exceptions everywhere**

Avoid:

```
throw new RuntimeException("Something went wrong");
```

Prefer meaningful application exceptions:

```
throw new PropertyNotFoundException(id);
```

Exception types communicate intent.

**Returning HTTP objects from the service**

Avoid:

```
public ResponseEntity<?> getPropertyById(...)
```

The service shouldn't decide whether something is:

```
404
409
403
```

as an HTTP response.

It should express application outcomes through return values or exceptions.

**Using exceptions for normal successful control flow**

Exceptions represent exceptional/failure paths.

Don't use them merely as an alternative to ordinary branching.

**Exposing stack traces to API clients**

A production response should not reveal things such as:

```
database passwords
SQL internals
package names
stack traces
server paths
```

Those belong in server-side logs, not public API responses.

**Mapping every exception to 400**

Different failures have different semantics.

```
Invalid input       → 400
Missing resource    → 404
Authorization       → 403
State conflict      → 409
Unexpected failure  → 500
```

Correct status codes make the API easier for clients to understand and handle.

## Engineering Trade-offs

Centralized exception handling gives us:

- thin controllers
- consistent responses
- reusable handling
- clear separation of concerns
- easier frontend integration

But centralization should not become one enormous class containing hundreds of unrelated rules.

In larger applications, exception handlers can be organized by concern while still using Spring's advice mechanism.

Another important trade-off concerns how much information errors expose.

Developers want detailed information for debugging.

Clients need useful but safe information.

So production systems often separate:

```
Internal logging
    ↓
Detailed exception information

External API response
    ↓
Stable, safe error contract
```

## Summary

The core architecture is:

```
Normal flow

Controller
    ↓
Service
    ↓
Repository
    ↓
Response
```

and:

```
Failure flow

Controller / Service / Framework
        ↓
Exception
        ↓
@RestControllerAdvice
        ↓
@ExceptionHandler
        ↓
Error DTO
        ↓
HTTP error response
```

Custom exceptions describe what failed:

```
PropertyNotFoundException
ViewingNotAllowedException
```

`@RestControllerAdvice` centralizes HTTP exception handling.

`@ExceptionHandler` maps specific exception types to responses.

Common framework failures include:

```
MethodArgumentNotValidException
→ Bean Validation failure

MethodArgumentTypeMismatchException
→ Path/query parameter conversion failure

HttpMessageNotReadableException
→ Request-body/deserialization failure
```

```
Bad JSON/body type
→ HttpMessageNotReadableException

Valid DTO created, but annotations fail
→ MethodArgumentNotValidException

Bad @PathVariable / @RequestParam type
→ MethodArgumentTypeMismatchException
```

And the most important separation is:

```
Service
→ expresses application failure

Exception
→ represents that failure

Global handler
→ translates failure into HTTP
```

This keeps business logic independent from the web layer while giving the API a consistent error contract.

## ErrorResponse DTO

A normal DTO class:

```
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;

    public ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
```

This is much safer and gives us a real API contract.

Java `record`

A record is a shorter way to represent this kind of simple immutable data object.

Instead of all that boilerplate:

```
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}
```

Java automatically gives you:

- private final fields
- constructor
- accessor methods
- equals()
- hashCode()
- toString()

Conceptually, this:

```
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}
```

is roughly replacing a much longer immutable DTO class.

Then the handler becomes:

```
ErrorResponse response = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        exception.getMessage()
);
```

and:

```
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(response);
```

Jackson can serialize that record into:

```
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Property not found with id: ..."
}
```

One syntax difference

With a traditional class, you normally access:

```
response.getStatus();
```

With a record, the generated accessor is:

```
response.status();
```

not:

```
response.getStatus();
```

The component name itself becomes the accessor method.

So:

```
public record ErrorResponse(
    int status,
    String message
) {}
```

gives:

```
response.status();
response.message();
```

Why records suit DTOs

Records are especially nice for objects whose job is simply:

**Carry this fixed set of data.**

Examples:

```
ErrorResponse
PropertySummaryResponse
ViewingResponse
```

They are concise and immutable by default.

The mental model is:

```
Map
→ flexible but loosely structured

Normal class DTO
→ strongly typed but verbose

record
→ strongly typed + concise + immutable
```
