# Spring Boot

> Your design + automatic object creation + automatic wiring
>
> Spring creates objects and connects them for you

**What you did**

```
UserRepository repo = new InMemoryUserRepository();
Notification notification = new EmailNotification();
UserService service = new UserService(repo, notification);
UserController controller = new UserController(service);
```

**What Spring does:**

```
It creates these objects for you automatically
```

**Core idea**

```
Spring = Dependency Injection container
```

**Mapping**

```
| Your concept | Spring            |
| ------------ | ----------------- |
| Controller   | `@RestController` |
| Service      | `@Service`        |
| Repository   | `@Repository`     |
| DI           | automatic         |
```

## Important concepts

```
Spring takes control of object creation (IoC)
and injects dependencies automatically (DI)
```

### 1. IoC — Inversion of Control

```
Before: YOU create objects
After: Spring creates objects
```

### 2. DI — Dependency Injection

```
Spring gives objects what they need
```

## Compare clearly

**❌ Without Spring**

```
UserService service = new UserService(
    new UserRepository(),
    new EmailNotification()
);
```

👉 tightly coupled

👉 hard to change

**✅ With Spring**

```
@Service
public class UserService {
    private UserRepository repo;
    private Notification notification;

    public UserService(UserRepository repo, Notification notification) {
        this.repo = repo;
        this.notification = notification;
    }
}
```

👉 Spring injects dependencies automatically

👉 no new needed

## 🔥 Why this is powerful

- loose coupling
- easier testing
- flexible design
- cleaner code

## Spring Flow

```
HTTP Request
   ↓
@RestController
   ↓
@Service
   ↓
@Repository
   ↓
Database
```

## CHECKPOINT

In spring boot

```
@RestController
public class WishlistController {

    private WishlistService service;

    public WishlistController(WishlistService service) {
        this.service = service;
    }

}
```

❓How does WishlistService get assigned WITHOUT writing new?

**Answer:**

Spring Boot uses its IoC container to create and manage objects. Instead of manually creating objects with new, Spring automatically instantiates the WishlistService and injects it into the WishlistController through dependency injection, typically via constructor injection.

### Behind the scene

```
1. Spring scans @Service → creates WishlistService object
2. Spring scans @RestController → creates WishlistController
3. Sees constructor needs WishlistService
4. Injects it automatically
```

❓Why is **constructor injection preferred** over:

```
@Autowired
private WishlistService service;
```

**Answer:**
Constructor injection is preferred because it makes **dependencies explicit(clearer)**, **supports immutability(safer)**, and improves **testability(testable)**. It also guarantees that all required dependencies are provided when the object is created, preventing invalid states.
