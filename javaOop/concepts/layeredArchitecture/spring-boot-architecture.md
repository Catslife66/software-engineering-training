# Spring Boot Architecture

## Spring Boot = same structure + annotations

**1. Controller**

```
@RestController
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String register(@RequestBody UserRequest request) {
        userService.register(request.getEmail(), request.getPassword());
        return "User registered";
    }
}
```

**2. Service**

```
@Service
public class UserService {

    private UserRepository userRepository;
    private Notification notification;

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

**3. Repository**

```
@Repository
public interface UserRepository {
    void save(User user);
}
```

**4. Notification (component)**

```
public interface Notification {
    void send(String message);
}

@Component
public class EmailNotification implements Notification {

    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}
```

## Key Annotations

| Annotation        | Meaning                          |
| ----------------- | -------------------------------- |
| `@RestController` | entry point (Controller)         |
| `@Service`        | business logic                   |
| `@Repository`     | data layer                       |
| `@Component`      | general object managed by Spring |

## Big Picture

```
Request → Controller
        → Service (brain)
        → Repository (data)
        → External systems (email, payment, etc.)
```

## Design practice

**🧩 Mini System: Subscription Service**

Features:

user subscribes to a plan
system saves subscription
system sends confirmation notification

Design structure:

- Controller
- Service
- Repository
- Interface(s)

Example structure:

```
User
|-- class User (fields: id, email, password)
|-- interface UserRepository (method: findById(String userId))


Subscription
|-- class Subscription (fields: id, userId, planId, startDate, endDate, status)
|-- class SubscriptionController ('/subscriptions')
|-- interface SubscriptionRepository
|   method: save(Subscription subscription)
|-- class SubscriptionService
|   fields:
|   - SubscriptionRepository subscriptionRepository
|   - UserRepository userRepository
|   - Notification notification
|   method:
|   - subscribe(String userId, String planId)
|       User user = userRepository.findById(userId)
|       Subscription subscription = new Subscription(userId, planId)
|       subscriptionRepository.save(subscription)
|       notification.send("Subscription created for user " + user.getEmail())

Notification
|-- interface Notification (method: send(String message))
|-- class EmailNotification implements Notification
```
