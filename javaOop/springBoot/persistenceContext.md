# Persistence Context

Where does Hibernate keep track of managed entities?

**Think of it as a workspace**

Suppose a transaction starts:

```
@Transactional
public void updateEmail(...) {
    ...
}
```

Hibernate creates a workspace:

```
Persistence Context
```

Inside that workspace Hibernate keeps track of:

```
Managed entities
Original values
Changes
Relationships
```

**Example**

```
User user =
    userRepository.findById(id);
```

Hibernate loads:

```
User(id=1, email=abc@example.com)
```

and places it into:

```
Persistence Context
```

Now the entity becomes:

```
Managed
```

**Later**

```
user.setEmail("new@example.com");
```

Hibernate notices:

```
Original:
abc@example.com

Current:
new@example.com
```

because both versions are being tracked in the persistence context.

This is how **dirty checking** works.

## Very Important Example

Suppose inside one transaction:

```
User user1 =
    userRepository.findById(1);

User user2 =
    userRepository.findById(1);
```

Question:

```
Should Hibernate execute:

SELECT * FROM users WHERE id = 1;

twice?
```

The answer is:

```
No
```

Because the persistence context already contains:

```
User(id=1)
```

Hibernate returns the same managed object.

## Why this is useful

Without it:

```
findById(1)
findById(1)
findById(1)
```

might hit the database three times.

With persistence context:

```
First query → database
Later queries → memory
```

within the same transaction.

## Example workflow

```
@Transactional
public void updateUser(UUID id) {

    User user =
        userRepository.findById(id);

    user.setEmail("new@example.com");

}
```

Flow:

```
Transaction starts
    ↓
Persistence Context created
    ↓
User loaded
    ↓
User becomes Managed
    ↓
Email changed
    ↓
Dirty checking detects change
    ↓
Transaction commits
    ↓
UPDATE generated
    ↓
Persistence Context destroyed
```

## Checkpoint Question

Imagine:

```
@Transactional
public void test() {

    User user1 =
        userRepository.findById(1);

    User user2 =
        userRepository.findById(1);

}
```

Question:

```
Why does Hibernate usually execute only one SELECT query here?
```

Answer:

```
Hibernate executes only one SELECT because after the first findById(1), the User entity is loaded into the Persistence Context and becomes a Managed Entity. When the second findById(1) occurs within the same transaction, Hibernate first checks the Persistence Context, finds that User already loaded, and returns the same managed entity instead of executing another database query.
```

### The Important Detail

Notice what Hibernate is really doing:

**First call**

```
User user1 =
    userRepository.findById(1);
```

Hibernate:

```
Persistence Context lookup
    ↓
Not found
    ↓
Execute SELECT
    ↓
Create Managed Entity
    ↓
Store in Persistence Context
```

**Second call**

```
User user2 =
    userRepository.findById(1);
```

Hibernate:

```
Persistence Context lookup
    ↓
Found User(id=1)
    ↓
Return existing Managed Entity
```

No SQL.

## Mental Model

Persistence Context guarantees:

```
One database row
        ↓
One managed entity instance
        ↓
Per transaction
```

This is a huge JPA concept.

```
Same persistence context + same entity ID
= same managed object instance
```

This is why the persistence context is often called the **first-level cache**.

## The First-Level Cache

A cache is simply:

```
Store data in memory
to avoid expensive work later

Expensive work = Database query
```

Because:

```
One Persistence Context
One Transaction
```

The cache only lives inside that context.

When transaction ends:

```
Persistence Context destroyed
```

Cache disappears.

Think:

```
Transaction-local cache
```

**Example Timeline**

Transaction starts:

```
Persistence Context created
```

Load User:

```
SELECT users...
```

Entity stored in cache.

Load same User again:

```
No SELECT
```

Transaction commits:

```
Persistence Context destroyed
```

Cache gone.

Suppose:

**Transaction A**

```
User user =
    userRepository.findById(1);

User user2 =
    userRepository.findById(1);
```

Email is:

```
abc@example.com
```

Entity is now inside Transaction A's Persistence Context.

Meanwhile:

**Transaction B**

Updates the same user:

```
email = updated@example.com
```

and commits.

Database now contains:

```
updated@example.com
```

Will Hibernate return for user2 -> abc@example.com ✅

**What Transaction A sees**

Transaction A:

```
User user1 =
    userRepository.findById(1);
```

Database at that moment:

```
abc@example.com
```

Hibernate loads:

```
User(id=1, email=abc@example.com)
```

and stores it in:

```
Persistence Context A
```

**What Transaction B does**

Later:

Transaction B

updates:

```
updated@example.com
```

and commits.

Database now contains:

```
updated@example.com
```

**What happens in Transaction A?**

Later:

```
User user2 =
    userRepository.findById(1);
```

Hibernate checks:

```
Persistence Context A
```

first.

Finds:

```
User(id=1, email=abc@example.com)
```

already managed.

Returns that entity.

No SQL.

### Why Hibernate does this

Because Hibernate guarantees:

```
One Entity ID
        ↓
One Managed Object
        ↓
Per Persistence Context
```

If Hibernate suddenly reloaded from database:

```
user1.email = abc@example.com

user2.email = updated@example.com
```

you would have:

```
Two versions of the same entity
inside one Persistence Context
```

which would be chaos.

This links directly back to:

```
Transaction Isolation
```

### Mental Model

Think:

Database

```
is the source of truth.
```

But:

```
Persistence Context
```

is Hibernate's working copy during the transaction.

Once an entity enters the Persistence Context:

```
Hibernate trusts its managed copy.
```

until the transaction ends.

## Complete Flow

```
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
@Transactional
    ↓
Persistence Context Created
    ↓
Repository.findById()
    ↓
Entity Loaded
    ↓
Managed Entity
    ↓
Modify Fields
    ↓
Dirty Checking
    ↓
Commit
    ↓
UPDATE SQL
    ↓
Persistence Context Destroyed
    ↓
Detached Entity
```
