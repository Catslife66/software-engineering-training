# JPA Foundations

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

### Key mental model

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

## Lazy Loading vs Eager Loading

Scenario:

```
public class Property {
    private UUID id;
    private String title;
    private User owner;
}

public class User {
    private UUID id;
    private String email;
}
```

You ask Hibernate:

```
Property property = propertyRepository.findById(id);
```

Question:

Should Hibernate immediately load:

```
Property

AND

Owner
```

from the database?

Or should it only load:

```
Property
```

and wait until you actually need the owner?

This leads to:

### Option 1 — Eager Loading

Immediately load everything.

```
Load Property
Load Owner
```

even if you never use owner.

Example

You only need:

```
property.getTitle();
```

but Hibernate still loads:

```
owner
email
passwordHash
etc
```

**Pros**

Simple.

Everything is already available.

**Cons**

Can load lots of unnecessary data.

Can become slow.

### Option 2 — Lazy Loading

Initially load only:

```
Property
```

When code later does:

```
property.getOwner()
```

Hibernate then loads:

```
Owner
```

from database.

**Pros**

Only load what you actually use.

More efficient.

**Cons**

Can create surprising database queries.

This is where the famous N+1 Problem comes from.

We'll learn that next.

Important❗️

**The decision is about what data does this use case need?**

### Real World Example

Suppose:

```
List<Property> properties = propertyRepository.findAll();
```

returns:

```
100 properties
```

Now:

```
for(Property p : properties){
    System.out.println(
        p.getOwner().getEmail()
    );
}
```

If owner is lazy loaded:

```
Query 1:
Load 100 properties

Query 2:
Load owner of property 1

Query 3:
Load owner of property 2

Query 4:
Load owner of property 3

...
```

Potentially:

```
101 database queries
```

for one page.

Very bad.

This is called **N+1 Query Problem**

## N+1 Problem

Imagine this code:

```
List<Property> properties = propertyRepository.findAll();

for (Property property : properties) {
    System.out.println(property.getOwner().getEmail());
}
```

Assume there are 100 properties.

What happens with lazy loading?

Query 1

Hibernate loads all properties:

```
SELECT * FROM properties;
```

That is 1 query.

Then inside the loop

For each property, Hibernate loads the owner:

```
SELECT * FROM users WHERE id = ?;
```

If there are 100 properties:

```
1 query for properties
+
100 queries for owners
=
101 queries
```

That is the N+1 problem.

```
N = number of records
+ 1 = original query
```

**Why it is bad**

It can make one API request secretly produce many database queries.

Small data:

```
10 properties → 11 queries
```

Large data:

```
1000 properties → 1001 queries
```

That becomes slow very quickly.

**Common solution**

If the use case needs owner data for a list, fetch them together.

Conceptually:

```
Load properties WITH owners
```

SQL idea:

```
SELECT p.*, u.*
FROM properties p
JOIN users u ON p.owner_id = u.id;
```

In JPA, this is often done with:

```
JOIN FETCH
```

or a DTO projection.

**Key rule**

```
Lazy loading is good until you accidentally lazy-load inside a loop.
```

That is the danger.

## Entity relationships vs DTO projections

The problem

Suppose the endpoint is:

```
GET /properties
```

The frontend needs:

```
title
price
city
agentName
```

A beginner approach might be:

```
List<Property> properties = propertyRepository.findAll();

for (Property property : properties) {
    String agentName = property.getOwner().getName();
}
```

This can cause N+1.

### Option 1 — Load full entities

This means loading:

```
Property entity
+
User owner entity
```

Useful when you need to modify domain objects or run business logic.

Example:

```
Edit property
Check ownership
Update property
Save property
```

Here, full entities make sense.

### Option 2 — DTO projection

This means the database query directly returns only the fields needed for the response:

```
title
price
city
agentName
```

Instead of loading full `Property` and full `User`.

This is often better for read-only list pages.

### Mental model

```
Use entities for business operations.
Use DTO projections for read-only views.

Entity loading = give me the object
DTO projection = give me the view
```

Example

For property search:

```
Use DTO projection
```

because we only need display data.

For editing property:

```
Use entity
```

because we need domain object + update behavior.

**Use DTO projection when:**

```
read-only response
only selected fields needed
list/detail display page
performance matters
```

**Use entity + fetched owner when:**

```
business rules need full entity
you will modify/save the entity
domain behavior matters
```
