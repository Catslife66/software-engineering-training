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

### Mapper

Why do we need a Mapper?

Imagine we don't have one.

Our service becomes:

```
@Transactional
public CreatePropertyResponse createProperty(CreatePropertyRequest request) {

    Property property = new Property(
        request.getTitle(),
        request.getCity(),
        request.getPrice(),
        request.getBedrooms()
    );

    Property saved = propertyRepository.save(property);

    return new CreatePropertyResponse(
        saved.getId(),
        saved.getTitle(),
        saved.getCity(),
        saved.getPrice(),
        saved.getBedrooms(),
        saved.getStatus(),
        saved.getCreatedAt()
    );
}
```

Notice what's happening.

The service is doing two jobs:

```
Business workflow
AND
Object conversion
```

That violates SRP.

**With a Mapper**

The service becomes much cleaner.

```
@Transactional
public CreatePropertyResponse createProperty(CreatePropertyRequest request) {

    Property property = propertyMapper.toEntity(request);

    Property saved = propertyRepository.save(property);

    return propertyMapper.toCreateResponse(saved);
}
```

Now the responsibilities are:

```
Service
↓
Business workflow

Mapper
↓
Convert objects
```

That's much cleaner.

**Where Does It Live?**

I recommend this project structure:

```
src/main/java/com/brightmove

├── config
├── controller
├── dto
│     ├── request
│     └── response
├── entity
├── enums
├── exception
├── mapper      ← here
├── repository
├── service
├── specification
├── validation
└── security
```

Inside mapper:

```
mapper
├── PropertyMapper.java
├── ViewingMapper.java
└── UserMapper.java
```

Simple and easy to navigate.

**What Does a Mapper Do?**

Every mapper answers two questions.

Request → Entity

```
CreatePropertyRequest
        ↓
Property
```

Used when:

```
POST /properties
```

Entity → Response

```
Property
        ↓
CreatePropertyResponse
```

Used when:

```
POST /properties
```

returns a response.

Later you'll also have:

```
Property
        ↓
PropertySummaryResponse

Property
        ↓
PropertyDetailResponse

Property
        ↓
PropertySearchResponse
```

The same entity can map to different DTOs depending on the endpoint.

#### One Thing We Should Decide

There are two common ways to write mappers.

**Option A — Manual Mapper**

```
public class PropertyMapper {

    public Property toEntity(CreatePropertyRequest request) {
        ...
    }

    public CreatePropertyResponse toCreateResponse(Property property) {
        ...
    }
}
```

Everything is plain Java.

Pros:

```
Easy to understand.
No extra libraries.
Great for learning.
```

**Option B — MapStruct**

A library generates most of the mapping code for you.

Pros:

```
Very little boilerplate.
Popular in many companies.
```

Cons:

```
Feels a bit like "magic" until you understand what it's generating
```
