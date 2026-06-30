# Read-only transactions

Sometimes a service only reads data:

```
@Transactional(readOnly = true)
public Page<PropertyListDTO> searchProperties(...) {
    return propertyRepository.findAll(spec, pageable)
            .map(this::toDto);
}
```

Meaning:

```
This method should not modify database state.
```

Why useful?

```
It communicates intent.
It can help Hibernate avoid unnecessary dirty-checking work.
It protects design clarity: read workflow vs write workflow.
```

But important:

```
readOnly = true is not your main security mechanism.
```

## Why Engineers Like `readOnly = true`

1. It documents intent

When another developer sees:

```
@Transactional(readOnly = true)
```

they immediately know:

```
This service should only query data.
```

This makes the codebase easier to understand.

2. It allows framework optimizations

Spring and Hibernate can optimize some behavior.

For example:

- Hibernate doesn't need to prepare for entity modifications in the same way.
- Some databases can optimize read-only transactions.

The exact optimization depends on the database and JPA provider, so think of this as a **possible performance benefit**, not a guarantee.

3. It helps catch design mistakes

Suppose someone later adds:

```
user.setEmail("new@email.com");
```

inside:

```
@Transactional(readOnly = true)
```

That should immediately make you think:

```
Why is a search method modifying a user?
```

Even if the framework doesn't always prevent it, it's a strong signal that the design is wrong.

## Read vs Write Services

You'll notice a pattern emerging.

**Read Service**

```
@Transactional(readOnly = true)
public Page<PropertyListDTO> search(...)
```

Responsibilities:

```
Read
Filter
Sort
Paginate
Map to DTO
```

**Write Service**

```
@Transactional
public void bookViewing(...)
```

Responsibilities:

```
Validate
Modify entities
Save business state
Commit
```

## This Is a Design Principle

Many large systems naturally separate services into:

```
Query Services
```

and

```
Command Services
```

This idea later evolves into an architectural pattern called **CQRS (Command Query Responsibility Segregation)**

Notice how your learning is connecting:

- Persistence teaches you how reads and writes differ.
- Architecture later teaches you why some systems separate them completely.

## The Complete Transaction Mental Model

Let's step back and see what you've learned in this chapter.

1. Transaction = Business Action

Not:

```
One SQL statement
```

Instead:

```
Book Viewing

Create Property

Approve Viewing

Transfer Money
```

2. Transaction Boundary

Usually:

```
Controller
      ↓
Service  ← Transaction starts
      ↓
Repository
```

The service owns the business workflow.

3. Commit

```
Everything succeeds
↓
Changes become permanent.
```

4. Rollback

```
Failure
↓
Undo every database change in the transaction.
```

5. Flush

```
Generate and send SQL
↓
Still reversible.
```

Commit is what makes changes permanent.

6. Exception Rules

Default:

```
RuntimeException
↓
Rollback
```

Default:

```
Checked Exception
↓
Commit
```

unless:

```
rollbackFor = ...
```

7. Swallowed Exceptions

```
catch (Exception e) {
    // ignore
}
↓
Spring sees no exception.
↓
Commit.
```

This is a classic production bug.

8. Propagation

```
REQUIRED
↓
Join existing transaction.
```

```
REQUIRES_NEW
↓
Create independent transaction.
```

9. readOnly = true

Use for:

```
Search
Reports
Dashboard
Details
Statistics
```

Normal transaction:

```
Create
Update
Delete
Approve
Book
Transfer
```
