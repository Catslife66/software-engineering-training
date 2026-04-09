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
