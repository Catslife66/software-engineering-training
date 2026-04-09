# Layered Architecture

## What problem are we solving?

When systems grow, we don’t want one class doing everything.

So we split responsibilities into layers:

```
Controller → Service → Repository
```

## What each layer does

**1. Controller (entry point)**
Receives requests from outside (user / API)

> Controller = presentation layer

Example:

```
"Register user"
```

Controller:

- receives input
- calls service
- returns result

👉 It should NOT contain business logic

Example:

```
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void register(String email, String password) {
        userService.register(email, password);
    }
}
```

**2. Service (business logic)**

Contains the real logic of the system

> Service = business logic layer

Example:

- validate input
- create user
- decide what happens next
- call repository
- call notification

👉 This is where your OOP + SOLID is heavily used

Example:

```
public class UserService {

    private UserRepository userRepository;
    private Notification notification;  // dependency injection

    public UserService(UserRepository userRepository, Notification notification) {
        this.userRepository = userRepository;
        this.notification = notification;
    }

    public void register(String email, String password) {
        User user = new User(email, password);

        userRepository.save(user);

        notification.send("Welcome " + email);
    }
}
```

**3. Repository (data layer)**
Handles data storage

Example:

- save user
- get user
- update user

👉 No business logic here

Example:

```
public interface UserRepository {
    void save(User user);
}

public class InMemoryUserRepository implements UserRepository {

    @Override
    public void save(User user) {
        System.out.println("User saved: " + user.getEmail());
    }
}

```

**Visual flow**

```
Controller → Service → Repository
                     ↓
                Notification
```

### Map this to Java

| FastAPI          | Java                         |
| ---------------- | ---------------------------- |
| `@app.post()`    | Controller                   |
| function logic   | Service                      |
| DB session logic | Repository                   |
| helper functions | Interfaces + implementations |
