# Spring Data JPA

So far our request can travel through:

```
HTTP Request
     ↓
Controller
     ↓
Service
```

Now we need persistence:

```
Controller
     ↓
Service
     ↓
Repository
     ↓
Database
```

Spring Data JPA provides the repository abstraction that lets our application work with persistent entities without writing repetitive SQL and JDBC(Java Database Connectivity) code for ordinary CRUD operations.

By the end of this module, the important ideas are:

- what JPA is
- what Spring Data JPA adds
- what a repository is
- how JpaRepository works
- CRUD operations
- Optional
- derived queries
- custom @Query methods

## Core Concepts

**JPA**

Jakarta Persistence API (JPA) is a specification/standard.

> "This is how Java ORM persistence should behave."

It defines concepts and APIs such as:

```
@Entity
@Id
@OneToMany
@ManyToOne
```

and persistence operations such as:

```
persist()
find()
remove()
```

plus entity lifecycle, persistence context, relationships, JPQL, etc.

JPA is primarily the **rules/API contract**, not the engine doing the work.

---

**Hibernate**

Hibernate is a JPA implementation.

It performs work such as:

```
Read @Entity mapping
        ↓
Track Java entities
        ↓
Generate SQL
        ↓
Use JDBC
        ↓
Communicate with PostgreSQL
```

JPA defines what `@Entity` and `@Id` mean.

Hibernate understands those annotations because it implements JPA.

Hibernate can then generate SQL and communicate with the database.

Hibernate understands entity mappings, tracks entities and generates SQL.

---

**Spring Data JPA**

Spring Data JPA provides a convenient repository layer on top of JPA.

Instead of writing:

```
entityManager.find(Property.class, id);
```

and building repository implementations ourselves, we write:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {
}
```

Spring Data generates the implementation.

Then we get:

```
propertyRepository.findById(id);
propertyRepository.save(property);
propertyRepository.findAll();
```

So the complete stack:

```
OUR APPLICATION
       │
       │ propertyRepository.findById(id)
       ▼
SPRING DATA JPA
       │
       │ repository abstraction
       ▼
JPA
       │
       │ persistence specification / contract
       ▼
HIBERNATE
       │
       │ JPA implementation / ORM engine
       ▼
JDBC
       │
       │ database communication
       ▼
POSTGRESQL
```

## Mental Model

Think of the repository as the application's persistence interface.

The service says:

```
propertyRepository.findById(id);
```

The service doesn't need to know whether Hibernate eventually executes something resembling:

```
SELECT *
FROM properties
WHERE id = ?;
```

Likewise:

```
propertyRepository.save(property);
```

might ultimately cause SQL such as:

```
INSERT INTO properties (...)
VALUES (...);
```

The repository gives the application a Java-oriented persistence API:

```
Business/Application Layer
          ↓
      Repository
          ↓
   Persistence mechanism
          ↓
       Database
```

## Spring / Java Implementation

`JpaRepository`

A basic repository looks like:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {
}
```

There is no class body.

Yet we can inject it:

```
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(
            PropertyRepository propertyRepository
    ) {
        this.propertyRepository = propertyRepository;
    }
}
```

How?

Spring Data creates the repository implementation for us at runtime and registers it as a Spring Bean.

So we write:

```
interface
```

and Spring supplies:

```
implementation
```

---

**The generic types**

This part:

```
JpaRepository<Property, UUID>
```

contains two generic types:

```
JpaRepository<EntityType, IdType>
```

Therefore:

```
JpaRepository<Property, UUID>
```

means:

```
Entity managed → Property

ID type        → UUID
```

If we had:

```
@Entity
public class Agent {

    @Id
    private Long id;
}
```

the repository would be:

```
public interface AgentRepository
        extends JpaRepository<Agent, Long> {
}
```

## CRUD Operations

Spring Data JPA provides these operations through repository methods.

**CREATE — save()**

Suppose we have:

```
Property property = new Property(
        "City Centre Flat",
        "Edinburgh",
        new BigDecimal("250000")
);
```

We can call:

```
propertyRepository.save(property);
```

At the persistence level, a new entity eventually results in an INSERT.

Conceptually:

```
Property object
     ↓
save()
     ↓
JPA / Hibernate
     ↓
INSERT
     ↓
properties table
```

Important detail:

save() does not simply mean SQL `INSERT`

It is a repository persistence operation whose behavior depends on whether the entity is considered new or existing.

---

**READ — findById()**

```
propertyRepository.findById(id);
```

returns:

```
Optional<Property>
```

not:

```
Property
```

Why?

Because the database might not contain that ID.

There are two legitimate outcomes:

```
Property exists
        ↓
Optional containing Property

Property doesn't exist
        ↓
Optional.empty()
```

---

**DELETE**

We can delete an entity:

```
propertyRepository.delete(property);
```

or by ID:

```
propertyRepository.deleteById(id);
```

However, there is an engineering difference between:

```
deleteById(id);
```

and:

```
Property property = propertyRepository.findById(id)
        .orElseThrow(
            () -> new PropertyNotFoundException(id)
        );

propertyRepository.delete(property);
```

The second form explicitly lets the service establish:

This property must exist before our business operation continues.

Which approach is appropriate depends on the required API/business semantics.

---

**UPDATE**

You may initially imagine:

```
propertyRepository.update(property);
```

But JpaRepository doesn't give us a general update() method.

You might see:

```
propertyRepository.save(property);
```

used for updates.

However, in a transactional Hibernate workflow, another important mechanism exists:

```
Dirty checking
```

For example:

```
@Transactional
public PropertyResponse updateProperty(
        UUID id,
        UpdatePropertyRequest request
) {

    Property property = propertyRepository.findById(id)
            .orElseThrow(
                () -> new PropertyNotFoundException(id)
            );

    property.setTitle(request.title());

    return propertyMapper.toResponse(property);
}
```

Notice:

```
No save(property)
```

When property is a managed entity inside the persistence context, Hibernate can detect the modification and synchronize it with the database.

---

`Optional<T>`

Optional explicitly represents:

There may or may not be a value.

Instead of:

```
Property property =
        propertyRepository.findById(id);
```

where property might mysteriously be null, the return type tells us absence is possible:

```
Optional<Property>
```

A common Spring service pattern is:

```
Property property = propertyRepository
        .findById(id)
        .orElseThrow(
            () -> new PropertyNotFoundException(id)
        );
```

Flow:

```
findById(id)
     ↓
Optional<Property>
     ↓
Does it contain Property?
     ├── yes → return Property
     │
     └── no → create and throw
              PropertyNotFoundException
```

This is why:

```
.orElseThrow(...)
```

appears so frequently in Spring services.

---

`findAll()`

```
List<Property> properties =
        propertyRepository.findAll();
```

returns all entities.

Conceptually:

```
SELECT *
FROM properties;
```

For large tables, however, loading everything is often undesirable.

That's why pagination exists.

---

`existsById()`

```
boolean exists =
        propertyRepository.existsById(id);
```

Useful when we only need to know whether something exists.

It expresses the intent better than loading an entire entity just to answer a yes/no question.

---

`count()`

```
long count = propertyRepository.count();
```

returns the number of entities.

## Query Derivation

CRUD methods aren't enough.

Suppose we need:

```
Find properties in Edinburgh.
```

Spring Data JPA lets us declare:

```
List<Property> findByCity(String city);
```

We don't implement it.

Spring examines the method name:

```
find
By
City
```

and derives a query from it.

Conceptually:

```
SELECT *
FROM properties
WHERE city = ?;
```

This feature is called query derivation.

Examples:

```
List<Property> findByBedrooms(Integer bedrooms);
```

Conceptually:

```
WHERE bedrooms = ?
```

```
List<Property> findByPriceLessThan(
        BigDecimal price
);
```

Conceptually:

```
WHERE price < ?
```

```
List<Property> findByCityAndBedrooms(
        String city,
        Integer bedrooms
);
```

Conceptually:

```
WHERE city = ?
AND bedrooms = ?
```

```
List<Property> findByCityOrderByPriceAsc(
        String city
);
```

Conceptually:

```
WHERE city = ?
ORDER BY price ASC
```

Spring understands keywords in repository method names.

Common examples include:

```
And
Or
Between
LessThan
GreaterThan
Like
Containing
StartingWith
EndingWith
OrderBy
True
False
IsNull
IsNotNull
```

## Repository Return Types

Repository queries can return different types depending on what the query means.

**One result may or may not exist**

```
Optional<Property> findByReference(
        String reference
);
```

This communicates:

```
0 or 1 expected
```

**Multiple results**

```
List<Property> findByCity(String city);
```

This communicates:

```
0, 1, or many
```

If no matching properties exist, the natural result is usually an empty list:

```
[]
```

rather than null.

**Boolean existence query**

```
boolean existsByReference(String reference);
```

This communicates:

```
I don't need the entity.
I only need to know whether it exists.
```

Repository method design therefore expresses intent.

## Custom `@Query` - JPQL

Derived queries are excellent when the query is straightforward.

But method names can eventually become ugly:

```
findByCityAndBedroomsGreaterThanAndPriceLessThanOrderByPriceAsc(...)
```

Spring Data lets us explicitly define queries.

For example:

```
@Query("""
    SELECT p
    FROM Property p
    WHERE p.city = :city
""")
List<Property> findPropertiesInCity(
        @Param("city") String city
);
```

This is JPQL.

Notice:

```
Property
p.city
```

rather than:

```
properties
city
```

JPQL queries entities and entity fields, not directly tables and columns.

For now, the important idea is simply:

```
Simple query
→ derived method often sufficient

More explicit/complex query
→ @Query
```

## Native Queries

Spring Data can also execute SQL directly:

```
@Query(
    value = """
        SELECT *
        FROM properties
        WHERE city = :city
    """,
    nativeQuery = true
)
List<Property> findByCityNative(
        @Param("city") String city
);
```

Now:

```
JPQL
→ talks in entities

Native SQL
→ talks directly in tables
```

Native SQL can be useful when database-specific functionality is needed, but it couples the query more closely to the database schema.

We generally shouldn't reach for native queries just because SQL feels familiar.

## Repository Design

A repository should represent persistence operations the application genuinely needs.

Do not declare the standard persistence operations yourself.

Such as:

```
save(...)
findById(...)
findAll()
delete(...)
deleteById(...)
existsById(...)
count()
```

Only the **application-specific data access operations** that aren't already provided.

For example:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {

    List<Property> findByCity(String city);

    Optional<Property> findByReference(String reference);

    boolean existsByReference(String reference);
}
```

And perhaps later:

```
@Query("""
    SELECT p
    FROM Property p
    WHERE p.agent.id = :agentId
      AND p.status = :status
""")
List<Property> findByAgentAndStatus(
        UUID agentId,
        PropertyStatus status
);
```

The repository shouldn't become a dumping ground for business logic.

For example, this responsibility:

```
Can this customer book this viewing?
```

doesn't belong in a repository.

A repository may answer factual data questions:

```
Does an existing viewing occupy this slot?
```

The service then uses that information to enforce the business rule.

Again:

```
Repository
→ data access

Service
→ business decision
```

## Request & Data Flow

Let's trace:

```
GET /properties/{id}
```

Controller:

```
@GetMapping("/{id}")
public PropertyResponse getProperty(
        @PathVariable UUID id
) {
    return propertyService.getPropertyById(id);
}
```

Service:

```
public PropertyResponse getPropertyById(UUID id) {

    Property property = propertyRepository
            .findById(id)
            .orElseThrow(
                () -> new PropertyNotFoundException(id)
            );

    return propertyMapper.toResponse(property);
}
```

Repository:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {
}
```

The complete conceptual flow is:

```
GET /properties/{id}
        ↓
PropertyController
        ↓
PropertyService
        ↓
propertyRepository.findById(id)
        ↓
Spring Data JPA
        ↓
JPA / Hibernate
        ↓
SQL
        ↓
PostgreSQL
        ↓
Property entity
        ↓
Optional<Property>
        ↓
Service
        ↓
PropertyResponse
        ↓
Controller
        ↓
JSON
```

Each layer has a different responsibility.

## BrightMove Example

A basic persistence structure might look like:

```
property/
├── Property.java
├── PropertyRepository.java
├── PropertyService.java
├── PropertyController.java
├── PropertyMapper.java
└── dto/
    ├── CreatePropertyRequest.java
    ├── UpdatePropertyRequest.java
    └── PropertyResponse.java
```

Repository:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {

    List<Property> findByCity(String city);

    Optional<Property> findByReference(String reference);

    boolean existsByReference(String reference);
}
```

Service:

```
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    public PropertyService(
            PropertyRepository propertyRepository,
            PropertyMapper propertyMapper
    ) {
        this.propertyRepository = propertyRepository;
        this.propertyMapper = propertyMapper;
    }

    public PropertyResponse getPropertyById(UUID id) {

        Property property = propertyRepository
                .findById(id)
                .orElseThrow(
                    () -> new PropertyNotFoundException(id)
                );

        return propertyMapper.toResponse(property);
    }
}
```

This gives us a clean separation:

```
Controller
→ HTTP

Service
→ application workflow

Repository
→ persistence

Entity
→ persistent domain state

DTO
→ API data
```

## Common Mistakes

**Thinking Spring Data JPA is Hibernate**

They work together, but they're different.

Keep this hierarchy:

```
Spring Data JPA
      ↓
JPA abstraction
      ↓
Hibernate implementation
      ↓
Database
```

---

**Calling .get() blindly on Optional**

Avoid:

```
Property property =
        propertyRepository.findById(id).get();
```

If the value doesn't exist, this throws a generic NoSuchElementException.

Prefer expressing the application meaning:

```
.orElseThrow(
    () -> new PropertyNotFoundException(id)
);
```

---

**Returning null collections**

Repository methods returning collections naturally use:

```
empty List
```

rather than forcing callers to constantly check for null.

---

**Creating repository implementations manually**

Don't write:

```
public class PropertyRepositoryImpl
        implements PropertyRepository {
    ...
}
```

for ordinary Spring Data CRUD.

Spring generates the implementation.

Custom implementations are possible, but only needed for more specialized requirements.

---

**Putting business logic in repositories**

Avoid designing repository methods around decisions such as:

```
approveViewingIfCustomerIsAllowed(...)
```

A repository accesses data.

The service coordinates the business workflow.

---

**Assuming save() is the whole persistence model**

This is particularly important.

Beginners often develop this mental model:

```
save()
→ database changes

no save()
→ database doesn't change
```

That is **not generally correct with JPA/Hibernate**.

Once entities become managed, dirty checking changes this picture completely.

That is exactly what we're learning next.

## Engineering Trade-offs

Spring Data JPA removes a large amount of persistence boilerplate.

Instead of manually writing:

```
open connection
prepare statement
bind parameters
execute SQL
read result set
map row to object
close resources
```

we can often write:

```
propertyRepository.findById(id);
```

This improves development speed and readability.

But abstraction has a cost.

It becomes easy to forget that underneath:

```
findAll()
```

there is real database work.

And underneath relationships, derived queries and entity access, Hibernate may generate SQL whose performance matters.

Therefore professional use of Spring Data JPA requires understanding **both sides**:

```
Convenient repository abstraction
            +
Understanding what persistence is doing underneath
```

That is why the next several modules deliberately go below the repository abstraction.

## Summary

The persistence stack is:

```
Service
   ↓
Spring Data JPA
   ↓
JPA
   ↓
Hibernate
   ↓
JDBC
   ↓
PostgreSQL
```

A repository such as:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {
}
```

automatically receives common persistence operations.

Important methods include:

```
save()
findById()
findAll()
existsById()
count()
delete()
deleteById()
```

findById() returns:

```
Optional<Property>
```

because the entity may not exist.

Query derivation lets method names describe simple queries:

```
findByCity(...)
findByCityAndBedrooms(...)
existsByReference(...)
```

For more explicit queries, Spring Data provides:

```
@Query
```

The repository answers persistence/data questions; the service makes business decisions.

Spring Data JPA makes persistence convenient, but Hibernate determines much of what actually happens to entities underneath that abstraction.

```
Standard CRUD
→ inherited JpaRepository methods

Simple application query
→ derived repository method

Specific/complex fixed query
→ @Query / JPQL

Many optional search filters
→ Specification
```

| Layer           | Main question                                                     |
| --------------- | ----------------------------------------------------------------- |
| PostgreSQL      | Where is the data stored?                                         |
| JDBC            | How does Java communicate with the database?                      |
| JPA             | What standard persistence/ORM API and rules should Java use?      |
| Hibernate       | Who implements those ORM rules and performs the persistence work? |
| Spring Data JPA | How can Spring make JPA repositories much easier to use?          |
| Repository      | What persistence operations does our application need?            |
