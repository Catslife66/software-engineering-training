# Global Exception Handling

## Central exception handler

It gives you:

```
less duplication
cleaner controllers
consistent error responses
easier maintenance
```

Instead of every controller deciding its own error shape, one global place decides:

```
PropertyNotFoundException → 404 Not Found
ForbiddenException → 403 Forbidden
ValidationException → 400 Bad Request
OptimisticLockException → 409 Conflict
UnexpectedException → 500 Internal Server Error
```

In Spring, that central place is:

```
@ControllerAdvice
```

or often:

```
@RestControllerAdvice
```

## @RestControllerAdvice

Spring gives us one class that sits outside every controller.

Conceptually:

```
                HTTP Request
                      │
                      ▼
                Controller
                      │
                      ▼
                 Service
                      │
             Exception thrown
                      │
                      ▼
         @RestControllerAdvice
                      │
                      ▼
             HTTP Error Response
```

Instead of writing:

```
try {
    ...
} catch (...) {
    ...
}
```

in every controller, we write one global handler.

Example:

```
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePropertyNotFound(
            PropertyNotFoundException ex) {

        ...
    }
}
```

## What a global exception handler should do

**Responsibility 1**

Choose the correct HTTP status.

**Responsibility 2**

Return a useful error response to the client.

For example:

```
{
    "success": false,
    "code": "PROPERTY_NOT_FOUND",
    "message": "The requested property was not found."
}
```

Why Not Return the Exception?

Imagine returning:

```
{
    "exception": "PropertyNotFoundException",
    "stackTrace": "...",
    "package": "com.brightmove..."
}
```

Problems:

- exposes internal implementation
- security risk
- confusing for API clients
- tightly couples the client to Java implementation details

Instead we return a stable API contract.

## A Dedicated Error DTO

Just as we use DTOs for successful responses:

```
PropertyDTO
ViewingDTO
UserDTO
```

we usually create one for errors too.

Example:

```
public class ErrorResponse {

    private String code;

    private String message;

}
```

Then every error in the application returns the same structure.

For example:

Validation

```
{
    "code": "VALIDATION_ERROR",
    "message": "Validation failed."
}
```

Property missing

```
{
    "code": "PROPERTY_NOT_FOUND",
    "message": "The requested property was not found."
}
```

Forbidden

```
{
    "code": "ACCESS_DENIED",
    "message": "You are not allowed to perform this action."
}
```

This Connects to DTO Design

Earlier you learned:

```
Entities
        ↓
DTOs
```

Now we add another DTO:

```
Success DTOs
PropertyDTO
ViewingDTO

↓

Error DTOs
ErrorResponse
```

So **both success and failure** have a well-defined API contract.

## A Typical Production DTO

```
public class ErrorResponse {

    private String code;

    private String message;

    private Integer status;

    private LocalDateTime timestamp;

    private String path;
}
```

| Field       | Who benefits?        |
| ----------- | -------------------- |
| `code`      | Frontend logic       |
| `message`   | User / developer     |
| `status`    | Client and debugging |
| `timestamp` | Operations / logs    |
| `path`      | Debugging            |

## One Handler Per Exception

Conceptually:

```
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePropertyNotFound(
            PropertyNotFoundException ex) {

        // build ErrorResponse

        // return 404

    }
}
```

We'll usually have methods like:

```
handlePropertyNotFound()

handleViewingNotFound()

handleValidation()

handleAccessDenied()

handleOptimisticLock()

handleUnexpected()
```

Each one maps:

```
Java Exception
        ↓
HTTP Status
        ↓
ErrorResponse
```

With one handler:

```
PropertyController
ViewingController
UserController
        │
        ▼
GlobalExceptionHandler
```

One place.

Consistent responses.

Easy to maintain.

This is another application of SRP:

```
Controllers → handle requests.
Services → execute business logic.
GlobalExceptionHandler → translate exceptions into HTTP responses.
```

## A Production-quality GlobalExceptionHandler

**Step 1 — The Skeleton**

A typical application has one class:

```
@RestControllerAdvice
public class GlobalExceptionHandler {

}
```

Think of this class as:

```
Application Error Translator
```

Its job is:

```
Java Exception
        ↓
HTTP Response
```

**Step 2 — Handle One Exception**

Suppose:

```
PropertyNotFoundException
```

We add one handler:

```
@ExceptionHandler(PropertyNotFoundException.class)
public ResponseEntity<ErrorResponse> handlePropertyNotFound(
        PropertyNotFoundException ex) {

    ...
}
```

Notice the method name.

It isn't:

```
exception1()
```

It describes **what it handles**.

**Step 3 — Build ErrorResponse**

Inside the method we build our DTO.

Conceptually:

```
ErrorResponse error = new ErrorResponse(
    "PROPERTY_NOT_FOUND",
    "The requested property was not found.",
    404
);
```

Notice something.

We are not returning:

```
PropertyNotFoundException
```

We are translating it into an API contract.

**Step 4 — Return HTTP Response**

Finally:

```
return ResponseEntity
       .status(404)
       .body(error);
```

The client receives:

```
404 Not Found

{
    "code": "PROPERTY_NOT_FOUND",
    "message": "The requested property was not found.",
    "status": 404
}
```

**Final Safety Net**

Every application should have one last handler:

```
@ExceptionHandler(Exception.class)
```

This is the catch-all.

If some unexpected bug occurs:

```
NullPointerException

IllegalStateException

IOException

...
```

and you haven't written a specific handler...

This method runs.

Response:

```
500 Internal Server Error

{
    "code": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred.",
    "status": 500
}
```

## Architecture So Far

```
HTTP Request
        │
        ▼
@RequestBody
        │
        ▼
@Valid
        │
        ▼
Controller
        │
        ▼
Service
(Business Validation)
        │
        ▼
@Transactional
        │
        ▼
Repository
        │
        ▼
Database
        │
        ▼
Exception?
        │
        ▼
@RestControllerAdvice
        │
        ▼
ErrorResponse
        │
        ▼
HTTP Response
```
