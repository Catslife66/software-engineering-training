# Result objects

> A structured response object (DTO / response model)

```
public class Result {
    private boolean success;
    private String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Static Factory Method -> gives MEANING to object creation
    // It creates an object — just like a constructor — but through a method.
    // use this instead of constructor because it is more readable and expressive.

    public static Result success(String message) {
        return new Result(true, message);
    }

    public static Result failure(String message) {
        return new Result(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
```

```
// Service -> returns structured result
public Result addToWishlist(...) {
    if (user == null) return Result.failure("User not found");
    ...
    return Result.success("Saved");
}


// Controller -> formats HTTP response

Result result = service.addToWishlist(...);

if (!result.isSuccess()) {
    return ResponseEntity.badRequest().body(result.getMessage());
}
```

## Typical Result structure in real systems

```
success  → boolean (true / false) quick check for frontend logic
message  → human-readable message (UI display)
code     → machine-readable error code (frontend logic, error handling)
data     → actual result (optional)
```

## End-to-End Flow (very important)

```
Frontend → Controller → Service → Repository
                               ↓
                           Result object
                               ↓
Controller → HTTP Response → Frontend
```

```
Service → returns Result (with code)
Controller:
    if success → 200
    if USER_NOT_FOUND → 404
    if DUPLICATE → 409
    if INVALID → 400
```
