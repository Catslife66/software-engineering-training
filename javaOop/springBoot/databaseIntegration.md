# Database Integration

This is one of the most important mindset shifts in backend engineering.

```
Fake persistence
(InMemoryRepository)
```

to

```
Real persistence
(PostgreSQL + JPA + Hibernate)
```

## Why is InMemoryRepository not enough?

So far we've used things like:

```
@Repository
public class InMemoryUserRepository implements UserRepository {
    private List<User> users = new ArrayList<>();
}
```

Question:

```
Where does the data live?
```

Answer:

```
RAM (memory)
```

**Problem 1 — Data disappears**

Imagine:

```
Register User
Add Wishlist
Create Booking
```

Then:

```
Application restarts
```

What happens?

```
All data disappears
```

because memory is cleared.

**Problem 2 — Multiple servers**

Imagine:

```
Server A
Server B
```

User registers through:

```
Server A
```

Data exists only inside:

```
Server A memory
```

Then next request goes to:

```
Server B
```

Server B knows nothing.

**Problem 3 — Queries become difficult**

Suppose we need:

```
Find all bookings for property 123
Find all users registered this month
Count viewings per agent
```

A database is much better at this than looping through Java collections.

## New architecture

Before:

```
Controller
→ Service
→ InMemoryRepository
→ ArrayList
```

After:

```
Controller
→ Service
→ Repository
→ PostgreSQL
```

Notice:

```
Service does NOT change
```

This is very important.

Because of DIP:

```
Depend on abstraction
```

Your service still uses:

```
UserRepository
```

interface.

## What is ORM?

ORM means:

```
Object Relational Mapping
```

This sounds scary but it's actually simple.

**Problem**

Java thinks in:

```
User user = new User(...)
```

Objects.

Database thinks in:

```
users table

id
email
password
```

Rows.

ORM translates between them.

```
Java Object
⇅
Database Row
```

**Example**

Java:

```
User user = new User(
    "abc@example.com",
    "password"
);
```

ORM converts to:

```
INSERT INTO users(email, password)
VALUES (...)
```

automatically.

## What is JPA?

JPA is:

```
Specification (rules)
```

Think:

```
Interface
```

not implementation.

Similar to:

```
UserRepository
```

being an interface.

JPA says:

```
This is how Java persistence should work.
```

But JPA itself does not do the work.

## What is Hibernate?

Hibernate is:

```
Implementation of JPA
```

Just like:

UserRepository

may be implemented by:

InMemoryUserRepository

Similarly:

JPA

is implemented by:

Hibernate

**Mental model**

```
JPA = Contract

Hibernate = Implementation of that contract
```

## What becomes an Entity?

```
public class User {
    private UUID id;
    private String email;
    private String password;
}
```

This is currently just:

```
Plain Java Object
```

Soon it becomes:

```
@Entity
public class User {
    ...
}
```

Now Hibernate knows:

```
This object should be stored in database.
```

## Repository changes

Currently:

```
public interface UserRepository {
    User findByEmail(String email);
    void save(User user);
}
```

and you manually implement:

```
InMemoryUserRepository
```

Later:

```
public interface UserRepository
    extends JpaRepository<User, UUID>
{
    Optional<User> findByEmail(String email);
}
```

Notice:

```
No implementation class
```

Spring/Hibernate generate it.

## Shift to JPA

```
public class User {
    private UUID id;
    private String email;
    private String password;
}
```

was just Domain Model

Now in JPA:

```
@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}
```

the same class becomes:

```
Domain Model
+
Database Mapping
```

## Key mental model

```
@Entity tells Hibernate:
"this Java class should become a database table mapping."
```

And:

```
@Id tells Hibernate:
"this field identifies one row."
```

| Concept                  | JPA annotation |
| ------------------------ | -------------- |
| This class maps to table | `@Entity`      |
| Table name               | `@Table`       |
| Primary key              | `@Id`          |
| Column settings          | `@Column`      |
