# Repository Layer with Spring Data JPA

Now we add a third player.

```
JPA
Hibernate
Spring Data JPA
```

Think:

```
JPA = Persistence Rules

Hibernate = Persistence Engine

Spring Data JPA = Convenience Layer
```

Without Spring Data JPA

You might write:

```
@Repository
public class JpaUserRepository {

    public User findByEmail(...) {
        ...
    }

    public void save(...) {
        ...
    }
}
```

Spring Data JPA instead of:

```
public class JpaUserRepository
```

you write:

```
public interface UserRepository
        extends JpaRepository<User, UUID> {
}
```

and Spring generates common repository implementations automatically.

using:

```
Entity metadata
+
JPA rules
+
Hibernate engine
```

## Why can Spring generate it?

Because Hibernate already knows:

```
How to:
INSERT
UPDATE
DELETE
SELECT
```

for:

```
@Entity
public class User
```

So Spring Data JPA can simply connect:

```
Repository Method
        ↓
Hibernate
        ↓
SQL
```

without you writing repository code.

## Compare With Your Old InMemory Repository

Earlier:

```
UserRepository
       ↓
InMemoryUserRepository
       ↓
ArrayList<User>
```

Now:

```
UserRepository
       ↓
Spring Data JPA
       ↓
Hibernate
       ↓
PostgreSQL
```

Same architecture.

Different implementation.

This is actually SOLID in action.

## What JpaRepository Gives You

When you write:

```
public interface UserRepository
        extends JpaRepository<User, UUID> {
}
```

Spring Data JPA automatically provides:

```
save(...)
findById(...)
findAll(...)
deleteById(...)
existsById(...)
count(...)
```

and many others.

You never write:

```
class JpaUserRepository
```

yourself.

Spring generates it at startup.

## Query Derivation

The method name derives the query.

Spring Data JPA understands the method name.

Spring reads:

```
find
    ↓
By
    ↓
Email
```

and interprets it as:

```
Query User
where email equals something
```

Example:

```
findByEmail(String email)
↓
SELECT *
FROM users
WHERE email = ?


findByEmailAndStatus(
    String email,
    String status
)
↓
SELECT *
FROM users
WHERE email = ?
AND status = ?
```

## Mental Model

```
Controller
    ↓
Service
    ↓
Repository Interface
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
PostgreSQL
```

## A Real Example

Suppose we have:

```
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    private String email;

    private String passwordHash;
}
```

Repository:

```
public interface UserRepository
        extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
}
```

Service:

```
public Result<Void> register(
        String email,
        String password) {

    User existingUser =
        userRepository.findByEmail(email);

    ...
}
```

Earlier, your repository looked like:

```
User findByEmail(String email);
```

Now Spring Data JPA usually returns:

```
Optional<User> findByEmail(String email);
```

instead of:

```
User findByEmail(String email);
```

Suppose:

```
abc@example.com
```

does not exist.

Why return Optional.empty() instead of null?

What if returns null?

Now:

```
System.out.println(
    user.getEmail()
);
```

Boom.

```
NullPointerException
```

**Optional Makes The Possibility Visible**

Instead:

```
Optional<User> user =
    userRepository.findByEmail(email);
```

The method signature itself says:

```
A user may exist.
A user may not exist.
```

The caller is forced to think about both cases.

Now we can write:

```
if(userOptional.isEmpty()) {
    return Result.failure(
        "USER_NOT_FOUND",
        "User not found."
    );
}
```

and then:

```
User user =
    userOptional.get();
```

## Repository Design Rule

Methods that may not find data often return:

```
Optional<User>

Optional<Property>

Optional<Viewing>
```

because:

```
Finding nothing is normal.
```

It's not an exception.

**Unique field**

```
Optional<User> findByEmail(String email);
```

Because:

```
0 or 1 result
```

**Primary key**

```
Optional<User> findById(UUID id);
```

Because:

```
0 or 1 result
```

**Non-unique field**

```
List<Property> findByCity(String city);
```

Because:

```
0 to many results
```
