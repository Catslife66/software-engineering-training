# Entity Lifecycle

Scenario:

```
User user = new User(
    "abc@example.com",
    "hashed-password"
);

userRepository.save(user);

user.setEmail("new@example.com");
```

Question:

Do we need userRepository.save(user)?

Or can Hibernate detect the change automatically? ✅

This is one of the famous JPA features called **Dirty Checking**

## Dirty Checking

With JPA:

Hibernate may track:

```
original state
current state
```

and automatically generate:

```
UPDATE users
SET email = 'new@example.com'
WHERE id = ...
```

during transaction commit.

When an entity becomes managed:

Hibernate writes down:

```
email = abc@example.com
```

Later:

```
user.setEmail("new@example.com");
```

Hibernate notices:

```
Old value:
abc@example.com

New value:
new@example.com
```

This difference is called:

**dirty state**

and Hibernate updates the database.

## Entity Lifecyle

```
new User()
        ↓
Transient

save()
        ↓
Managed

modify fields
        ↓
Dirty

commit transaction
        ↓
UPDATE generated
```

## Important distinction

This only works for:

```
Managed entities
```

not every object.

For example:

```
User user = new User(...);
```

Hibernate is not tracking this object yet.

It's just:

```
Transient
```

(an ordinary Java object).

Only after it becomes managed by Hibernate can dirty checking occur.

## Detached Entities

Imagine:

```
User user = userRepository.findById(id);
```

Hibernate loads the entity.

State:

```
Managed
```

Then:

```
Transaction commits
Request ends
```

Should Hibernate keep tracking this object forever?

No, because:

```
The request is finished.
The persistence context is closed.
Hibernate's job is done.
```

**What happens instead?**

After the transaction/persistence context ends:

```
Managed
    ↓
Detached
```

The object still exists:

```
user.setEmail("new@example.com");
```

is perfectly legal Java.

But now:

```
Hibernate is no longer watching it.
```

So:

```
user.setEmail("new@example.com");
```

does not trigger dirty checking anymore.

## Lifecycle So Far

```
new User()
    ↓
Transient

save()
    ↓
Managed

modify fields
    ↓
Dirty

commit
    ↓
Database updated

transaction ends
    ↓
Detached
```

Example

Transaction 1

```
User user = userRepository.findById(id);

user.setEmail("first@example.com");
```

Commit:

```
UPDATE users
SET email = 'first@example.com'
```

Works because:

```
Managed
```

Later

```
user.setEmail("second@example.com");
```

But the transaction is already finished.

State:

```
Detached
```

Result:

```
No UPDATE
```

Hibernate isn't tracking it anymore.

## Mental Model

**Transient**

```
User user = new User(...);
```

Hibernate:

```
Knows nothing about it
```

No database tracking.

**Managed**

```
User user = userRepository.findById(id);

or

userRepository.save(user);
```

Hibernate:

```
Tracks the entity
```

Dirty checking works.

**Dirty**

```
user.setEmail("new@example.com");
```

while Managed.

Hibernate:

```
Detects changes
```

**Commit**

Transaction commits.

Hibernate generates:

```
UPDATE users
SET email = ...
```

**Detached**

Transaction ends.

Persistence context closes.

Hibernate:

```
Stops tracking the entity
```

No more dirty checking.

## Big Picture Architecture

```
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Transaction Starts
    ↓
Repository
    ↓
Hibernate loads Entity
    ↓
Managed State
    ↓
Business Logic changes Entity
    ↓
Dirty Checking
    ↓
Transaction Commit
    ↓
SQL Generated
    ↓
Persistence Context Closed
    ↓
Detached State
```
