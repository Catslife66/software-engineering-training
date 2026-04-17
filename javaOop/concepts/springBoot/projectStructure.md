# Spring Boot Project Structure

A typical Spring Boot project looks like this:

```
com.example.app
│
├── controller
├── service
├── repository
├── model
└── AppApplication.java
```

Example: Wishlist Project Structure

Let’s map your wishlist system:

```
wishlist-app
│
├── controller
│   └── WishlistController.java
│
├── service
│   └── WishlistService.java
│
├── repository
│   ├── UserRepository.java
│   ├── PropertyRepository.java
│   └── WishlistRepository.java
│
├── model
│   ├── User.java
│   ├── Property.java
│   └── WishlistItem.java
│
└── WishlistAppApplication.java
```

## What each part does

`Controller`

```
@RestController
public class WishlistController { ... }
```

👉 entry point for HTTP requests

`Service`

```
@Service
public class WishlistService { ... }
```

👉 your business logic lives here

`Repository`

```
@Repository
public interface WishlistRepository { ... }
```

👉 talks to database (or fake data for now)

`Model`

```
public class WishlistItem { ... }
```

👉 represents data

## The “magic” file

WishlistAppApplication.java

```
@SpringBootApplication
public class WishlistAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(WishlistAppApplication.class, args);
    }
}
```

**What this does**

```
Starts Spring
Scans your project
Creates objects
Injects dependencies
Runs server
```

**Important concept**

```
@Component Scan
```

Spring looks for:

- @RestController
- @Service
- @Repository

👉 and creates them automatically

## What you DON’T see (but happens)

When app starts:

```
1. Spring scans all classes
2. Finds annotations
3. Creates objects (beans)
4. Stores them in container
5. Injects dependencies
```

**Key term**

> Bean = object managed by Spring

Spring injects a UserRepository by scanning for a class that implements the UserRepository interface and is annotated (e.g. with @Repository). It creates that implementation as a bean and injects it into the service during application startup.

**Visual flow**

```
UserRepository (interface)
        ↑
InMemoryUserRepository (@Repository)

Spring:
→ creates InMemoryUserRepository
→ injects into WishlistService
```

**Question**

Why is it better for WishlistService to depend on UserRepository (interface) instead of InMemoryUserRepository (class)?

```
It is better for WishlistService to depend on the UserRepository interface rather than a concrete implementation because it follows the Dependency Inversion Principle. This allows the service to remain independent of specific implementations. Spring can then inject any class that implements the interface at runtime, making the system flexible, easier to extend, and easier to test without modifying existing code.
```

## How HTTP request data gets into your controller?

`@PathVariable`

is used to extract values from the URL path and bind them to method parameters in a controller. It allows the controller to access dynamic parts of the URL, such as userId and propertyId.

**Example**

URL:

```
POST /users/123/wishlist/456
```

Controller:

```
@PostMapping("/users/{userId}/wishlist/{propertyId}")
public ResponseEntity<Result> add(
    @PathVariable String userId,
    @PathVariable String propertyId
)
```

What happens:

```
{userId} → "123"
{propertyId} → "456"
```

Spring automatically:

```
extracts → converts → assigns to parameters
```

**Key idea**

> @PathVariable = take values from URL path

## 3 common ways to get data in controllers

| Annotation    | Source              |
| ------------- | ------------------- |
| @PathVariable | URL path            |
| @RequestParam | query string        |
| @RequestBody  | request body (JSON) |

**Example**

PathVariable

```
/users/123
```

RequestParam

```
/users?id=123
```

RequestBody

> @RequestBody is used when the client needs to send structured data, such as JSON, which is mapped to a Java object for processing.

```
{
  "userId": "123"
}
```

## DTOs

Request and response data often have different purposes from domain objects, so forcing them to share the same structure reduces flexibility and can leak sensitive fields. DTOs keeps the API contract separate from internal models, which improves safety, maintainability, and design clarity.

Example:

Request body

```
{
  "userId": "123",
  "propertyId": "456"
}
```

DTO class

```
public class WishlistRequest {
    private String userId;
    private String propertyId;

    public String getUserId() {
        return userId;
    }

    public String getPropertyId() {
        return propertyId;
    }
}
```

Controller

```
@PostMapping("/wishlist")
public ResponseEntity<Result> add(@RequestBody WishlistRequest request) {
    return service.addToWishlist(
        request.getUserId(),
        request.getPropertyId()
    );
}
```

**Key idea**

```
DTO → handles input
Entity → represents data

Controller → DTO (input)
Service → domain objects
Repository → persistence
```

### The key distinction DTO vs Entity

**DTO**

For communication

- request input
- response output

**Domain model / entity**

For internal business and persistence logic

- system state
- database mapping
- internal rules

**One sentence to remember**

DTOs protect the boundary of your system.
