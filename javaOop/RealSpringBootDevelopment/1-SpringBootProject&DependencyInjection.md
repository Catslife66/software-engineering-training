# Spring Boot Project & Dependency Injection

The purpose of this module is to understand how a Spring Boot application is structured and how objects are created and connected together.

Before Spring, we manually created objects using new.

After Spring, the framework is responsible for creating, managing and connecting objects. This concept is called **Dependency Injection (DI)** and is one of the foundations of the Spring Framework.

By the end of this module you should understand:

- how a Spring Boot application starts
- what a Bean is
- what the Spring IoC Container does
- how Dependency Injection works
- the purpose of Spring stereotypes (@Service, @Repository, etc.)
- why constructor injection is preferred

## Core Concepts

### Spring Boot

Spring Boot is a framework built on top of Spring.

Its goal is to simplify backend application development by providing:

- automatic configuration
- dependency management
- embedded web server
- production-ready defaults

Instead of configuring everything manually, Spring Boot makes sensible decisions for you.

### The IoC Container

The heart of Spring is the **Inversion of Control (IoC) Container**.

Think of it as a factory responsible for creating and managing objects.

Without Spring:

```
PropertyRepository repository = new PropertyRepository();
PropertyService service = new PropertyService(repository);
PropertyController controller = new PropertyController(service);
```

You are responsible for creating every object.

With Spring:

```
@RestController
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }
}
```

Spring automatically creates:

- PropertyRepository
- PropertyService
- PropertyController

and connects them together.

You never write new PropertyService(...).

### Bean

A Bean is simply an object managed by Spring.

Not every Java object is a Bean.

Example:

```
Property property = new Property(...);
```

This is not a Bean.

It is a normal Java object (a domain entity).

However:

```
@Service
public class PropertyService {
}
```

This class becomes a Bean because Spring creates and manages it.

Typical Beans are:

- Controllers
- Services
- Repositories
- Configuration classes

### Dependency Injection

A dependency is simply another object a class needs.

Dependency Injection means:

A class receives its dependencies instead of creating them itself.

Example:

```
PropertyController
        │
        ▼
PropertyService
        │
        ▼
PropertyRepository
```

The controller depends on the service.

The service depends on the repository.

## Mental Model

Think of Spring as a company.

```
Application starts
        │
        ▼
Spring reads annotations
        │
        ▼
Creates Beans
        │
        ▼
Stores them in the IoC Container
        │
        ▼
Injects dependencies
        │
        ▼
Application ready
```

The IoC Container is like a company HR department.

Instead of every employee hiring their own colleagues, HR assigns the correct people to each team.

Your classes simply declare what they need.

Spring supplies it.

## Spring / Java Implementation

`@SpringBootApplication`

This is the application's entry point.

```
@SpringBootApplication
public class BrightMoveApplication {

    public static void main(String[] args) {
        SpringApplication.run(
            BrightMoveApplication.class,
            args
        );
    }
}
```

SpringApplication.run(...) performs several tasks:

- starts the IoC Container
- scans the project for Beans
- creates the Beans
- injects dependencies
- starts the embedded server (Tomcat by default)

**Component Scanning**

Spring searches the project for classes annotated with stereotypes.

Examples:

```
@Component
@Service
@Repository
@RestController
```

Every discovered class becomes a Bean.

**Stereotype Annotations**

`@Component`

The most general stereotype.

Use for infrastructure or utility classes.

`@Service`

Marks business logic.

Example:

```
@Service
public class PropertyService {
}
```

`@Repository`

Marks persistence classes.

Example:

```
@Repository
public class PropertyRepository {
}
```

When extending `JpaRepository`, Spring automatically creates the repository implementation, so an explicit @Repository annotation is usually unnecessary.

`@RestController`

Marks REST controllers.

These receive HTTP requests and return HTTP responses.

**Constructor Injection**

Preferred approach:

```
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }
}
```

Spring sees:

```
PropertyRepository propertyRepository
```

looks inside the IoC Container,

finds the corresponding Bean,

and injects it.

## Request & Data Flow

A typical request flows like this:

```
HTTP Request
        │
        ▼
Controller Bean
        │
        ▼
Service Bean
        │
        ▼
Repository Bean
        │
        ▼
Database
        │
        ▼
Repository
        │
        ▼
Service
        │
        ▼
Controller
        │
        ▼
HTTP Response
```

Notice:

- Controllers do not create Services.
- Services do not create Repositories.

Everything is supplied by Spring.

## BrightMove Example

Our project already follows this architecture.

```
PropertyController
        │
        ▼
PropertyService
        │
        ▼
PropertyRepository
        │
        ▼
PostgreSQL
```

When a request arrives:

```
POST /properties
```

the controller receives the request,

passes it to the service,

the service performs business logic,

the repository saves the entity,

and the response flows back up.

Every layer has a single responsibility.

## Common Mistakes

**Creating dependencies manually**

Incorrect:

```
PropertyService service = new PropertyService();
```

Inside a controller.

This bypasses Spring completely.

**Field Injection**

```
@Autowired
private PropertyService propertyService;
```

Although supported, constructor injection is preferred because:

- dependencies are explicit
- fields can be final
- easier to test
- immutable design

**Mixing responsibilities**

Controllers should not contain business logic.

Repositories should not contain validation.

Services should coordinate business workflows.

## Engineering Trade-offs

Dependency Injection provides:

Advantages:

- Loose coupling
- Easier testing
- Better maintainability
- Clear architecture
- Centralized object lifecycle

Trade-offs:

- Requires understanding the IoC Container
- Object creation becomes less explicit
- Framework magic can confuse beginners

However, the benefits become substantial in medium and large applications.

## Summary

**Key concepts**

- Spring Boot simplifies Spring configuration.
- The IoC Container creates and manages Beans.
- A Bean is an object managed by Spring.
- Dependency Injection means receiving dependencies rather than creating them.
- Constructor Injection is the recommended approach.
- Controllers, Services and Repositories each have distinct responsibilities.
- The request flows through the layers while Spring manages object creation behind the scenes.
