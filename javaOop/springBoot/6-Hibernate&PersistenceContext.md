# Hibernate & Persistence Context

We learned how our application interacts with persistence through Spring Data JPA:

```
propertyRepository.findById(id);
propertyRepository.save(property);
```

Those methods are convenient, but they hide something important.

Consider:

```
@Transactional
public void changePrice(UUID id, BigDecimal newPrice) {

    Property property = propertyRepository.findById(id)
            .orElseThrow(
                () -> new PropertyNotFoundException(id)
            );

    property.setPrice(newPrice);
}
```

There is no:

```
propertyRepository.save(property);
```

Yet Hibernate can still update the database.

Why?

To understand that, we need to go beneath the repository abstraction and understand:

- entities
- persistence context
- entity lifecycle states
- managed entities
- dirty checking
- flush
- commit

This is one of the most important mental models in JPA/Hibernate.

## Core Concepts

**ORM**

Hibernate is an **Object-Relational Mapping (ORM)** framework.

Our Java application thinks in objects:

```
Property property;
property.getCity();
property.setPrice(...);
```

The database thinks in tables and rows:

```
properties

id | title | city | price
```

Hibernate maps between them:

```
Java                         Database

Property class      ↔       properties table

property.id         ↔       id column
property.city       ↔       city column
property.price      ↔       price column
```

But Hibernate does considerably more than convert rows into objects.

It also tracks entity state.

That leads us to the persistence context.

## Mental Model — Persistence Context

The persistence context is an environment in which Hibernate keeps track of managed entities.

Imagine we load:

```
Property property =
        propertyRepository.findById(id)
                .orElseThrow(...);
```

Inside an active persistence context:

```
DATABASE
   │
   │ SELECT
   ▼
Property object
   │
   ▼
PERSISTENCE CONTEXT
   │
   └── tracks this Property
```

The object is no longer simply an ordinary Java object from Hibernate's perspective.

It is a **managed entity**.

Hibernate knows:

```
Which database row it represents

What its state was when loaded

What its current state is

Whether it has changed
```

This tracking is what makes dirty checking possible.

## Entity Lifecycle

An entity can exist in several lifecycle states.

The four important ones are:

- Transient
- Managed
- Detached
- Removed

Understanding these states explains much of Hibernate's seemingly "magical" behaviour.

### Transient

Consider:

```
Property property = new Property();

property.setTitle("City Centre Flat");
property.setCity("Edinburgh");
```

This is currently just a Java object.

Hibernate isn't tracking it.

There may be no corresponding database row.

```
Java Heap

Property object
     ↑
 TRANSIENT


Persistence Context
     ✗

Database
     ✗
```

A transient entity is therefore:

**An entity object that exists in Java but is not currently managed by the persistence context.**

### Managed

Now suppose the entity becomes associated with the persistence context.

For example, we load it:

```
Property property =
        propertyRepository.findById(id)
                .orElseThrow(...);
```

Within the transaction/persistence context, Hibernate tracks it.

```
Database row
     ↕
Property object
     ↕
Persistence Context
```

The entity is now:

```
MANAGED
```

This is the most important state for understanding Hibernate.

When a managed entity changes:

```
property.setPrice(
        new BigDecimal("275000")
);
```

Hibernate can notice.

### Detached

Suppose the persistence context ends.

For example:

@Transactional
public Property loadProperty(UUID id) {

    return propertyRepository.findById(id)
            .orElseThrow(...);

}

Once the transaction/persistence context is over, the returned entity may become detached.

The Java object still exists:

```
Property object
     ✓
```

The database row still exists:

```
Database row
     ✓
```

But Hibernate is no longer tracking that particular object:

```
Persistence Context
     ✗
```

So:

```
property.setPrice(...);
```

on a detached entity **does not automatically mean Hibernate will update the database**.

The key difference is:

```
Managed
→ Hibernate is tracking it.

Detached
→ Object still exists, but Hibernate is no longer tracking it.
```

### Removed

Suppose a managed entity is marked for deletion:

```
propertyRepository.delete(property);
```

It enters a removed state.

Conceptually:

```
Managed Property
      ↓
delete()
      ↓
Removed
      ↓
flush
      ↓
DELETE FROM properties ...
```

The entity exists temporarily in the persistence context but is scheduled for deletion from the database.

### Entity Lifecycle Map

Put this diagram in your notes:

```
              new Property()
                    │
                    ▼
               TRANSIENT
                    │
             persist/save
                    │
                    ▼
                MANAGED
               /       \
              /         \
 persistence context     remove
       ends                │
        │                  ▼
        ▼               REMOVED
     DETACHED
```

A detached entity can also be merged back into a persistence context, which we'll touch on shortly.

## Persistence Context Identity

The persistence context does more than track changes.

It also maintains identity.

Suppose within the same persistence context we request the same entity twice:

```
Property property1 =
        propertyRepository.findById(id)
                .orElseThrow(...);

Property property2 =
        propertyRepository.findById(id)
                .orElseThrow(...);
```

Conceptually, Hibernate can recognize:

```
I already have Property ID 123 managed.
```

The persistence context acts as a **first-level cache**.

The important mental model is:

```
Persistence Context

ID 123 → Property object
ID 456 → Property object
ID 789 → Property object
```

For a particular entity identity within one persistence context, Hibernate maintains a single managed representation.

This helps guarantee consistent object identity within a unit of work.

## Dirty Checking

Now we can explain one of Hibernate's most important features.

Consider:

```
@Transactional
public void updatePrice(
        UUID id,
        BigDecimal newPrice
) {

    Property property =
            propertyRepository.findById(id)
                    .orElseThrow(
                        () -> new PropertyNotFoundException(id)
                    );

    property.setPrice(newPrice);
}
```

There is no:

```
propertyRepository.save(property);
```

Yet the update can still happen.

Why?

Because property is **managed**.

Hibernate tracks it.

Conceptually, when loaded:

```
Original state

price = 250000
```

Later:

```
property.setPrice(
        new BigDecimal("275000")
);
```

Current state:

```
price = 275000
```

Hibernate detects:

```
Original
250000
    ≠
Current
275000
```

The entity is **dirty**.

Hibernate can therefore generate:

```
UPDATE properties
SET price = 275000
WHERE id = ?;
```

This mechanism is called **Dirty Checking**.

---

**Why save() isn't required here**

This is a very important distinction.

For a managed entity:

```
find entity
    ↓
entity becomes managed
    ↓
modify entity
    ↓
Hibernate tracks change
    ↓
flush
    ↓
UPDATE
```

Therefore:

```
property.setPrice(newPrice);
```

is sufficient inside the appropriate persistence context.

Calling:

```
propertyRepository.save(property);
```

again is usually unnecessary for this particular managed-entity update workflow.

## What does save() actually do?

This requires some nuance.

Spring Data JPA's:

```
repository.save(entity)
```

ultimately chooses between JPA operations corresponding roughly to:

```
persist
or
merge
```

depending on whether Spring Data considers the entity new.

Conceptually:

**New entity**

```
Transient
   ↓
save()
   ↓
persist
   ↓
Managed
   ↓
INSERT
```

**Existing/detached entity**

```
Detached
   ↓
save()
   ↓
merge
   ↓
Managed representation
   ↓
possible UPDATE
```

But `merge()` has an important subtlety.

Suppose:

```
Property detached = ...;

Property managed =
        entityManager.merge(detached);
```

The detached object itself does not magically become managed.

Instead, Hibernate copies its state into a managed instance.

Conceptually:

```
Detached object
      │
      │ copy state
      ▼
Managed object
      │
      ▼
Persistence Context
```

This distinction becomes useful when debugging detached-entity problems.

## Flush

Dirty checking explains how Hibernate knows an entity changed.

But when does SQL actually reach the database?

This brings us to **flush**.

Flush means:

**Synchronize changes in the persistence context with the database.**

Suppose:

```
property.setPrice(newPrice);
```

At first, we're modifying the managed Java object.

Conceptually:

```
Persistence Context

Property
price = 275000
```

The database may still currently contain:

```
Database

price = 250000
```

When Hibernate flushes:

```
Persistence Context
       ↓
   dirty checking
       ↓
 SQL generated
       ↓
   Database
```

Now the database receives:

```
UPDATE properties
SET price = 275000
WHERE id = ?;
```

---

**Flush does NOT mean commit**

This distinction is critical.

```
FLUSH
=
Synchronize persistence-context changes
with the database transaction
```

Whereas:

```
COMMIT
=
Successfully finalize the transaction
```

You can conceptually have:

```
change entity
    ↓
flush
    ↓
SQL UPDATE sent
    ↓
something fails
    ↓
ROLLBACK
```

Even though SQL was sent during flush, the transaction can still be rolled back.

Therefore:

**Flush does not mean the transaction has permanently succeeded.**

---

**When does flush happen?**

Hibernate may flush at several points.

Most importantly:

```
Before transaction commit
```

It may also flush before certain queries when synchronization is necessary.

You can explicitly request a flush:

```
propertyRepository.flush();
```

or:

```
propertyRepository.saveAndFlush(property);
```

But ordinary application code usually **shouldn't manually flush without a specific reason**.

Hibernate normally manages it appropriately.

## Commit

Commit belongs to the database transaction.

Consider:

```
@Transactional
public void updatePrice(...) {

    Property property = ...;

    property.setPrice(newPrice);
}
```

A simplified flow is:

```
Transaction begins
        ↓
SELECT Property
        ↓
Property becomes managed
        ↓
setPrice()
        ↓
Managed entity becomes dirty
        ↓
Transaction finishing
        ↓
Flush
        ↓
UPDATE SQL
        ↓
Commit
        ↓
Transaction successful
```

If something fails before successful commit:

```
Transaction
    ↓
failure
    ↓
rollback
```

The database changes don't become committed.

We'll study transaction behaviour properly in Transactions & Concurrency.

For now:

```
Dirty Checking
→ detects entity changes

Flush
→ synchronizes changes with DB

Commit
→ finalises the transaction
```

## Persistence Context and @Transactional

In normal Spring applications, transaction boundaries and persistence contexts are closely related.

Consider:

```
@Transactional
public PropertyResponse updateProperty(
        UUID id,
        UpdatePropertyRequest request
) {

    Property property =
            propertyRepository.findById(id)
                    .orElseThrow(...);

    property.setTitle(request.title());

    return propertyMapper.toResponse(property);
}
```

The useful mental model is:

```
@Transactional method begins
        ↓
transaction + persistence context available
        ↓
findById()
        ↓
Property managed
        ↓
modify Property
        ↓
dirty checking
        ↓
flush
        ↓
commit
        ↓
transaction/persistence context ends
```

There are technical nuances to exactly how persistence contexts and transactions can be scoped, but this is the correct model for the ordinary Spring service workflows we're building.

## Request & Data Flow

Let's trace a BrightMove update.

Request:

```
PUT /properties/{id}
```

Controller:

```
@PutMapping("/{id}")
public PropertyResponse updateProperty(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePropertyRequest request
) {
    return propertyService.updateProperty(id, request);
}
```

Service:

```
@Transactional
public PropertyResponse updateProperty(
        UUID id,
        UpdatePropertyRequest request
) {

    Property property =
            propertyRepository.findById(id)
                    .orElseThrow(
                        () -> new PropertyNotFoundException(id)
                    );

    property.setTitle(request.title());
    property.setPrice(request.price());

    return propertyMapper.toResponse(property);
}
```

Persistence flow:

```
HTTP Request
      ↓
Controller
      ↓
@Transactional Service
      ↓
Transaction begins
      ↓
Repository.findById()
      ↓
Hibernate executes SELECT
      ↓
Property enters persistence context
      ↓
MANAGED
      ↓
setTitle()
setPrice()
      ↓
Property becomes dirty
      ↓
Service finishes
      ↓
Hibernate dirty checking
      ↓
Flush
      ↓
UPDATE SQL
      ↓
Commit
      ↓
Response
```

Notice again:

```
No repository.save(property)
```

is necessary for the managed entity in this workflow.

## BrightMove Example

A typical service might eventually contain:

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

    @Transactional
    public PropertyResponse updateProperty(
            UUID id,
            UpdatePropertyRequest request
    ) {

        Property property =
                propertyRepository.findById(id)
                        .orElseThrow(
                            () -> new PropertyNotFoundException(id)
                        );

        property.setTitle(request.title());
        property.setPrice(request.price());

        return propertyMapper.toResponse(property);
    }
}
```

The key isn't the syntax.

The key is understanding the invisible state:

```
propertyRepository.findById()
             ↓
         Property
             ↓
          MANAGED
             ↓
property.setPrice(...)
             ↓
           DIRTY
             ↓
           FLUSH
             ↓
          UPDATE
```

Once you understand that chain, Hibernate becomes much less mysterious.

## Common Mistakes

**Thinking every Java object is managed**

This:

```
Property property = new Property();
```

creates a transient object.

Hibernate isn't automatically tracking every instance of an entity class.

---

**Thinking @Entity means the object is currently managed**

@Entity tells JPA:

Objects of this class can participate in persistence.

It does not mean every Property object is currently managed.

State depends on its lifecycle and persistence context.

---

**Calling save() after every setter**

This is often unnecessary:

```
Property property =
        propertyRepository.findById(id)
                .orElseThrow(...);

property.setPrice(newPrice);

propertyRepository.save(property);
```

If property is already managed in the transaction, dirty checking handles the update.

This code may still work, but it can hide whether you actually understand the lifecycle.

---

**Assuming detached objects are still tracked**

```
property.setPrice(newPrice);
```

only participates in automatic dirty checking if that entity instance is managed.

A detached object's changes are not automatically synchronized just because its class has @Entity.

---

**Confusing flush and commit**

Never think:

```
flush = saved permanently
```

Instead:

```
flush
→ SQL synchronized within current transaction

commit
→ transaction finalised
```

---

**Thinking the persistence context is the database**

It isn't.

There are two different states:

```
Java/Hibernate state
        ↓
Persistence Context

Persistent storage
        ↓
Database
```

Flush synchronizes them.

## Engineering Trade-offs

Hibernate's persistence context is powerful because we can work naturally with objects.

Instead of writing:

```
UPDATE properties
SET price = ...
WHERE id = ...
```

throughout our business logic, we work with:

```
property.setPrice(newPrice);
```

Hibernate tracks the change.

That allows business code to focus more on domain state.

But the abstraction has a cost.

Developers can forget:

```
Object access may trigger database work.

Relationships may trigger queries.

Managed entities are tracked.

Flush may generate SQL.

Transactions determine consistency.
```

This is why professional Hibernate development requires understanding what's happening beneath repository calls.

## Summary

The four important entity lifecycle states are:

```
TRANSIENT
Java object exists, not managed

MANAGED
Tracked by persistence context

DETACHED
Previously managed, no longer tracked

REMOVED
Managed and scheduled for deletion
```

The most important state is:

```
MANAGED
```

because it enables:

```
Dirty Checking
```

The central update flow is:

```
find entity
     ↓
MANAGED
     ↓
modify entity
     ↓
DIRTY
     ↓
flush
     ↓
UPDATE SQL
     ↓
commit
```

And three terms must remain distinct:

```
Dirty Checking
→ Hibernate detects changes to managed entities

Flush
→ synchronizes persistence-context changes with database

Commit
→ successfully finalises the database transaction
```

Finally, connect this module to the previous one:

```
Spring Data JPA
"What repository operations can I conveniently use?"

                ↓

Hibernate & Persistence Context
"What happens to my entities underneath those operations?"
```

That distinction is the foundation for almost everything coming next: entity mappings, relationships, transactions, optimistic locking, lazy loading and N+1.
