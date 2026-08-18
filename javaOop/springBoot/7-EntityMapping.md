# Entity Mapping

Hibernate manages entity objects through the persistence context.

But Hibernate first needs to know:

How does this Java class correspond to database data?

For example:

```
Java                         Database

Property                    properties
--------                    ----------
id              ↔           id
title           ↔           title
price           ↔           price
status          ↔           status
version         ↔           version
```

Entity mapping defines this relationship.

This module covers:

- @Entity
- @Table
- @Id
- ID generation
- @Column
- enum mapping
- lifecycle callbacks
- @Version

## Core Concept

**Entity**

A JPA entity is a Java class representing persistent application data.

Example:

```
@Entity
public class Property {

    @Id
    private UUID id;

    private String title;

    private BigDecimal price;
}
```

@Entity tells JPA:

**Objects of this class can be persisted as entities.**

Hibernate reads this metadata and maps objects to database rows.

Conceptually:

```
Property object

id = 123
title = "City Centre Flat"
price = 250000

        ↕

properties table

123 | City Centre Flat | 250000
```

@Entity makes a class persistence-capable.
It does not mean every object created from that class is automatically managed.

This:

```
Property property = new Property();
```

is still initially transient.

## `@Entity`

Basic syntax:

```
@Entity
public class Property {
}
```

The annotation comes from Jakarta Persistence:

```
import jakarta.persistence.Entity;
```

By default, JPA uses the entity class name as its entity name.

The entity name matters particularly in JPQL:

```
SELECT p
FROM Property p
```

Here:

```
Property
```

refers to the entity, not directly to the database table.

## `@Table`

We can explicitly define the database table:

```
@Entity
@Table(name = "properties")
public class Property {
}
```

Now:

```
Java entity
Property

        ↕

Database table
properties
```

This makes the mapping explicit.

@Table can also define database-level constraints.

For example:

```
@Table(
    name = "properties",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_property_reference",
            columnNames = "reference"
        )
    }
)
```

This tells the schema that property references should be unique.

Application validation and database constraints serve different purposes:

```
Application validation
→ friendly early rejection

Database constraint
→ final data-integrity protection
```

The database should still protect invariants that must always hold.

## `@Id`

Every entity needs an identifier.

```
@Id
private UUID id;
```

The ID uniquely identifies the entity.

Conceptually:

```
Property entity
     ↓
id
     ↓
Database primary key
```

Hibernate also uses identity heavily inside the persistence context:

```
Property + ID 123
        ↓
managed entity identity
```

## ID Generation

We usually don't want application callers manually inventing database IDs.

There are several strategies.

For numeric IDs, you may see:

```
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

or:

```
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE)
private Long id;
```

The exact strategy interacts with the database.

UUID IDs

Modern Jakarta Persistence/Hibernate setups can support generated UUIDs, for example:

```
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

Conceptually:

```
new Property
    ↓
ID generated
    ↓
persisted with UUID
```

UUIDs have useful properties for distributed applications because IDs don't require one central numeric sequence.

But they also have trade-offs such as larger indexes compared with small numeric keys.

## @Column

Basic fields are often mapped automatically:

```
private String title;
```

Hibernate can infer a column.

But `@Column` lets us specify database mapping details:

```
@Column(name = "title")
private String title;
```

We can also specify constraints:

```
@Column(
    nullable = false,
    length = 150
)
private String title;
```

Conceptually:

```
Java
String title

       ↕

Database
title VARCHAR(150) NOT NULL
```

**Unique Columns**

For example:

```
@Column(
    nullable = false,
    unique = true
)
private String reference;
```

This expresses a uniqueness constraint.

For more complex or explicitly named constraints, `@Table(uniqueConstraints = ...)` is often preferable.

## Validation vs Entity Mapping

This is worth separating carefully.

Suppose our request DTO has:

```
@NotBlank
@Size(max = 150)
private String title;
```

And our entity has:

```
@Column(
    nullable = false,
    length = 150
)
private String title;
```

These look similar, but they operate at different layers.

```
@NotBlank
@Size
     ↓
API/input validation

@Column
database constraint/mapping
     ↓
Persistence layer
```

For example:

```
@NotBlank
```

means:

```
Reject blank user input.
```

Whereas:

```
@Column(nullable = false)
```

describes:

```
This persistent column should not contain SQL NULL.
```

These are not interchangeable.

A useful layered model is:

```
Request DTO
   ↓
Bean Validation
   ↓
Service / business rules
   ↓
Entity
   ↓
Database constraints
```

## Mapping Java Types

Hibernate maps common Java types to suitable database types.

Examples:

```
Java                   Database concept

String                 VARCHAR/TEXT
Integer                INTEGER
Long                   BIGINT
BigDecimal             NUMERIC/DECIMAL
Boolean                BOOLEAN
LocalDate              DATE
LocalDateTime          TIMESTAMP
UUID                   UUID
```

Exact SQL types depend on the database and configuration.

For money-like values, BigDecimal is generally preferable to floating-point types such as double.

Example:

```
@Column(
    precision = 12,
    scale = 2
)
private BigDecimal price;
```

Conceptually:

```
9999999999.99
```

with controlled decimal precision.

## Enum Mapping

Suppose BrightMove has:

```
public enum PropertyStatus {
    AVAILABLE,
    UNDER_OFFER,
    SOLD
}
```

and:

```
private PropertyStatus status;
```

We need to decide how that enum is stored.

JPA provides:

```
@Enumerated
```

There are two major approaches.

**EnumType.ORDINAL**

```
@Enumerated(EnumType.ORDINAL)
private PropertyStatus status;
```

This stores numeric positions:

```
AVAILABLE   → 0
UNDER_OFFER → 1
SOLD        → 2
```

This is risky.

Suppose later we change the enum:

```
public enum PropertyStatus {
    AVAILABLE,
    RESERVED,
    UNDER_OFFER,
    SOLD
}
```

Now the positions change.

Existing database values can acquire the wrong meaning.

**EnumType.STRING**

Prefer:

```
@Enumerated(EnumType.STRING)
private PropertyStatus status;
```

Database:

```
AVAILABLE
UNDER_OFFER
SOLD
```

This is much safer and easier to inspect.

So a strong default rule is:

Persist enums using `EnumType.STRING` unless you have a specific reason not to.

## Entity Constructors

JPA entities require a no-argument constructor accessible to the persistence provider, conventionally:

```
protected Property() {
}
```

Then we can provide constructors representing valid application creation:

```
public Property(
        String title,
        String city,
        BigDecimal price
) {
    this.title = title;
    this.city = city;
    this.price = price;
    this.status = PropertyStatus.AVAILABLE;
}
```

Using:

```
protected Property() {
}
```

rather than making it public communicates:

**This constructor exists primarily for JPA, not normal application usage.**

## Entity Lifecycle Callbacks

Sometimes we want code to execute automatically at particular persistence lifecycle events.

JPA provides callbacks such as:

```
@PrePersist
@PostPersist
@PreUpdate
@PostUpdate
@PreRemove
@PostRemove
@PostLoad
```

For example, timestamps.

```
@Column(nullable = false)
private LocalDateTime createdAt;

private LocalDateTime updatedAt;
```

Then:

```
@PrePersist
protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();

    createdAt = now;
    updatedAt = now;
}
```

Before first persistence:

```
persist
   ↓
@PrePersist
   ↓
INSERT
```

For updates:

```
@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

Flow:

```
managed entity changed
       ↓
flush
       ↓
@PreUpdate
       ↓
UPDATE
```

## When to Use Lifecycle Callbacks

Lifecycle callbacks are useful for persistence-related bookkeeping such as:

```
createdAt
updatedAt
```

But they should not become hidden business workflow engines.

Avoid putting substantial logic such as:

```
charge customer
send email
create viewing
change ownership
```

inside:

```
@PrePersist
```

or:

```
@PreUpdate
```

Because these callbacks are tied to persistence lifecycle events, not explicit business use cases.

Business workflows belong in services/domain logic where they're visible and controllable.

## `@Version`

Now we reach an annotation we've previously used:

```
@Version
private Long version;
```

This enables **optimistic locking**.

Suppose a property initially contains:

```
id      = 123
price   = £250,000
version = 3
```

User A and User B both read version 3.

```
             Database
             version 3
             /       \
            /         \
        User A       User B
        version 3    version 3
```

User A updates first.

Hibernate performs an update conceptually like:

```
UPDATE properties
SET price = 260000,
    version = 4
WHERE id = 123
  AND version = 3;
```

It succeeds.

Database now contains:

```
version = 4
```

User B still has version 3.

User B attempts:

```
UPDATE properties
SET price = 270000,
    version = 4
WHERE id = 123
  AND version = 3;
```

But:

```
database version = 4
```

so:

```
WHERE version = 3
```

matches zero rows.

Hibernate detects the stale update and reports an optimistic locking failure.

This prevents User B from silently overwriting User A's change.

## Why @Version Belongs to Entity Mapping

At first, optimistic locking sounds like a transaction topic.

And we'll study the full concurrency behaviour in Module 9.

But `@Version` itself belongs here because it changes how the entity maps to persistent state.

Entity:

```
@Version
private Long version;
```

Database:

```
version column
```

Hibernate uses that mapping later to implement concurrency control.

So for this module, remember:

```
@Version
→ declares versioned entity state
```

## BrightMove Example

Putting the concepts together:

```
@Entity
@Table(
    name = "properties",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_property_reference",
            columnNames = "reference"
        )
    }
)
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
        nullable = false,
        unique = true
    )
    private String reference;

    @Column(
        nullable = false,
        length = 150
    )
    private String title;

    @Column(nullable = false)
    private String city;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status;

    @Version
    private Long version;

    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false
    )
    private LocalDateTime updatedAt;

    protected Property() {
    }

    public Property(
            String reference,
            String title,
            String city,
            BigDecimal price
    ) {
        this.reference = reference;
        this.title = title;
        this.city = city;
        this.price = price;
        this.status = PropertyStatus.AVAILABLE;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // domain methods / getters
}
```

Now we can read this class almost as a persistence contract:

```
Property
    ↓
stored in properties

id
    ↓
primary key / generated UUID

reference
    ↓
required + unique

price
    ↓
decimal persistent value

status
    ↓
enum stored as text

version
    ↓
optimistic locking state

createdAt / updatedAt
    ↓
lifecycle-maintained timestamps
```

## Request & Data Flow

Suppose BrightMove creates a property.

The controller receives:

```
{
  "reference": "BM-10293",
  "title": "City Centre Flat",
  "city": "Edinburgh",
  "price": 250000
}
```

DTO validation occurs.

The service creates:

```
Property property = new Property(
        request.reference(),
        request.title(),
        request.city(),
        request.price()
);
```

At this point:

```
Property
   ↓
TRANSIENT
```

Then:

```
propertyRepository.save(property);
```

Hibernate uses the entity mapping:

```
@Entity
@Table
@Id
@Column
@Enumerated
...
```

to understand how that object should be persisted.

Before insertion:

```
@PrePersist
```

runs.

Then the entity becomes managed and its state is synchronized to the database.

Conceptually:

```
HTTP
 ↓
DTO
 ↓
Service
 ↓
new Property
 ↓
TRANSIENT
 ↓
repository.save()
 ↓
Hibernate reads mapping
 ↓
@PrePersist
 ↓
MANAGED
 ↓
INSERT
 ↓
PostgreSQL
```

## Common Mistakes

**Treating entities as request DTOs**

Avoid:

```
@PostMapping
public Property create(
        @RequestBody Property property
)
```

An entity represents persistent/domain state.

A request DTO represents the external API contract.

Keep them separate.

---

**Using EnumType.ORDINAL casually**

Avoid:

```
@Enumerated(EnumType.ORDINAL)
```

unless you specifically understand and accept the consequences.

Prefer:

```
@Enumerated(EnumType.STRING)
```

---

**Thinking @Column(nullable = false) replaces @NotNull**

They operate at different boundaries.

```
@NotNull
→ validation

@Column(nullable = false)
→ persistence/database mapping
```

Often both may be appropriate.

---

**Putting business workflows in lifecycle callbacks**

Keep:

```
@PrePersist
@PreUpdate
```

for small persistence lifecycle concerns.

Don't hide major business operations there.

---

**Exposing setters for everything automatically**

JPA doesn't require every entity field to have a public setter.

For example, instead of:

```
property.setStatus(PropertyStatus.SOLD);
```

a richer entity might expose:

```
property.markAsSold();
```

The second method can enforce entity-level rules.

Entity mapping doesn't require abandoning good OOP design.

---

**Confusing entity class with managed entity**

Again:

```
@Entity
public class Property
```

means the class is persistence-capable.

But:

```
new Property(...)
```

is initially transient.

Lifecycle state determines whether Hibernate is currently tracking the object.

## Engineering Trade-offs

JPA annotations let us express persistence mapping directly beside the entity:

```
@Entity
@Column
@Enumerated
@Version
```

This is convenient because the mapping is easy to see.

But it also means the domain entity knows something about persistence technology.

That is an architectural trade-off.

In many Spring Boot business applications, this is entirely reasonable because the simplicity is valuable.

More strongly isolated architectures may separate domain models from persistence entities, but that adds additional mapping and complexity.

For BrightMove, using JPA entities as our persistent domain model is an appropriate trade-off.

Understand what coupling you're introducing and whether the additional abstraction would actually benefit the system.

## Summary

Entity mapping answers:

How should persistent Java state correspond to database state?

The main annotations from this module are:

```
@Entity
@Table
@Id
@GeneratedValue
@Column
@Enumerated
@PrePersist
@PreUpdate
@Version
```

Keep these distinctions clear:

```
@Entity
→ class participates in JPA persistence

@Table
→ database table mapping

@Id
→ entity identity / primary key

@Column
→ field-to-column mapping

@Enumerated(EnumType.STRING)
→ safe enum persistence

Lifecycle callbacks
→ small persistence lifecycle actions

@Version
→ version state used for optimistic locking
```

And connect the last three modules:

```
5-Spring Data JPA
        ↓
How do I access persistence conveniently?

6-Hibernate & Persistence Context
        ↓
How are entity objects tracked?

7-Entity Mapping
        ↓
How does Hibernate know what those objects mean in the database?
```

We now understand individual entities.

The natural next problem is:

```
Property belongs to Agent.

Property has Viewings.

Viewing belongs to Property.
```

That requires mapping **connections between entities**.

Q&A session:

1. in the section of '16. A Complete BrightMove Entity', why updatedAt field doesn't have @Column annotation?
2. in the section of '13. When to Use Lifecycle Callbacks', you mentioned createdAt and updatedAt should not become hidden business workflow engines.
   i'm not quite sure what it means? Do you mean when an object is created or updated inside persistence context, we dont need to manually call onCreate and onUpdate. Will Hibernate detect it and populate the data?
