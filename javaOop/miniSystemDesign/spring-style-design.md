# Build your FIRST Spring-style system

👉🏻 Build 'POST /wishlist'

## 1. The goal

We want this flow:

POST /users/{userId}/wishlist/{propertyId}

Meaning:

- user wants to save a property
- system checks user exists
- system checks property exists
- system checks duplicate
- system saves wishlist item
- controller returns response

## 2. The structure

We will create these pieces:

```
WishlistController
WishlistService
UserRepository
PropertyRepository
WishlistRepository
Result
User
Property
WishlistItem
```

## 3. Domain classes

Data models

`User`

```
public class User {
    private String id;
    private String email;

    public User(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
```

`Property`

```
public class Property {
    private String id;
    private String title;

    public Property(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
```

`WishlistItem`

```
public class WishlistItem {
    private String userId;
    private String propertyId;

    public WishlistItem(String userId, String propertyId) {
        this.userId = userId;
        this.propertyId = propertyId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPropertyId() {
        return propertyId;
    }
}
```

## 4. Result class - web layer

structured outcome returned by the service

```
public class Result {
    private boolean success;
    private String code;
    private String message;

    public Result(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static Result success(String message) {
        return new Result(true, "SUCCESS", message);
    }

    public static Result failure(String code, String message) {
        return new Result(false, code, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
```

## 5. Repository interfaces

Abstraction for data access

`UserRepository`

```
public interface UserRepository {
    User findById(String userId);
}
```

`PropertyRepository`

```
public interface PropertyRepository {
    Property findById(String propertyId);
}
```

`WishlistRepository`

```
public interface WishlistRepository {
    WishlistItem findByUserAndProperty(String userId, String propertyId);
    void save(WishlistItem item);
}
```

## 6. Service

business logic

```
import org.springframework.stereotype.Service;

@Service
public class WishlistService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final WishlistRepository wishlistRepository;

    public WishlistService(
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            WishlistRepository wishlistRepository
    ) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.wishlistRepository = wishlistRepository;
    }

    public Result addToWishlist(String userId, String propertyId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Result.failure("USER_NOT_FOUND", "User does not exist.");
        }

        Property property = propertyRepository.findById(propertyId);
        if (property == null) {
            return Result.failure("PROPERTY_NOT_FOUND", "Property does not exist.");
        }

        WishlistItem existing = wishlistRepository.findByUserAndProperty(userId, propertyId);
        if (existing != null) {
            return Result.failure("WISHLIST_DUPLICATE", "Property already saved.");
        }

        WishlistItem item = new WishlistItem(userId, propertyId);
        wishlistRepository.save(item);

        return Result.success("Property saved to wishlist.");
    }
}
```

## 7. Controller

receives the HTTP request and converts Result into HTTP responses

```
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{userId}/wishlist/{propertyId}")
    public ResponseEntity<Result> addToWishlist(
            @PathVariable String userId,
            @PathVariable String propertyId
    ) {
        Result result = wishlistService.addToWishlist(userId, propertyId);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }

        if ("USER_NOT_FOUND".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if ("PROPERTY_NOT_FOUND".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if ("WISHLIST_DUPLICATE".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
```

## 8. What happens in the flow

Let’s walk through it slowly.

### Case 1: success

Request comes in:

```
POST /users/u1/wishlist/p1
```

Flow:

- controller receives request
- controller calls service
- service checks user exists
- service checks property exists
- service checks duplicate
- service saves item
- service returns Result.success(...)
- controller returns 200 OK

### Case 2: user missing

Flow:

- controller calls service
- service finds no user
- service returns:
  ```
  Result.failure("USER_NOT_FOUND", "User does not exist.")
  ```
- controller maps that to:
  ```
  404 Not Found
  ```

### Case 3: duplicate item

Flow:

- controller calls service
- service sees item already exists
- service returns failure result
- controller maps it to:
  ```
  409 Conflict
  ```

## 9. What each layer is responsible for

**Controller**

- receive HTTP request
- call service
- return HTTP response

**Service**

- apply business rules
- decide success/failure
- return structured result

**Repository**

- access data source

**Result**

- represent outcome

That separation is the important thing.

## 10. Why constructor injection appears everywhere

You now see this pattern:

```
public WishlistController(WishlistService wishlistService)
```

and

```
public WishlistService(UserRepository ..., PropertyRepository ..., WishlistRepository ...)
```

Spring sees those constructors and injects the needed objects automatically.

So this is:

- IoC
- DI
- loose coupling
- testable design

all working together.

## 11. Important note

This example is intentionally simplified.

In a real Spring Boot app:

- domain classes may be JPA entities
- repositories may extend `JpaRepository`
- you may use DTOs for requests/responses
- exceptions may be handled with @ControllerAdvice

But the design idea is the same.

## 12. What you should notice most

This single example uses almost everything you learned:

- **Encapsulation**: fields inside classes
- **Abstraction**: repository interfaces
- **Polymorphism**: repository implementations can vary
- **Composition**: service has repositories
- **SRP**: each class has one responsibility
- **DIP**: service depends on interfaces
- **Layered architecture**: controller → service → repository

That’s why this is such a good milestone example.
