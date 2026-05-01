# Transactions & Isolation (ACID in practice)

**What is a Transaction?**

> a group of SQL operations that must succeed or fail together

## Concurrency Problem

**1. Lost Update**
Scenario

Balance = 100

Two users:

```
A reads 100
B reads 100
A writes 80
B writes 90
```

Final balance = 90 ❌ (should be 70 or 80 depending)

**2. Dirty Read**

```
Transaction A updates value (not committed)
Transaction B reads it
Transaction A rolls back
```

👉 B read invalid data ❌

**3. Non-repeatable Read**

```
A reads value = 100
B updates to 200
A reads again → 200
```

👉 same query, different result

**4. Phantom Read**

```
A reads rows WHERE status='active'
B inserts new row
A reads again → new row appears
```

## Isolation Levels

Databases solve this with isolation levels

**1. READ UNCOMMITTED**

```
can see uncommitted data ❌
```

Dirty reads allowed

**2. READ COMMITTED (most common)**

```
only see committed data
```

But:

- non-repeatable reads possible
- phantom reads possible

**3. REPEATABLE READ**

```
same rows stay consistent
```

But:

- phantom reads still possible

**4. SERIALIZABLE (strongest)**

```
as if transactions run one by one
```

- no anomalies
- but slower

## Real Intuition

| Level           | Safe   | Performance |
| --------------- | ------ | ----------- |
| Read Committed  | medium | fast        |
| Repeatable Read | safer  | medium      |
| Serializable    | safest | slow        |

```
READ COMMITTED → each query is fresh
REPEATABLE READ → transaction sees frozen snapshot
SERIALIZABLE → transaction sees isolated world
```

## Real-world implication

**Example: checking stock**

```
SELECT stock FROM products WHERE id = 1;
```

If two users do this:

```
User A sees stock = 1
User B sees stock = 1
```

Both buy → stock becomes -1 ❌

**Solution**

Use:

```
transactions + locking
```

## Row Locking (very important)

Example

```
SELECT * FROM products
WHERE id = 1
FOR UPDATE;
```

This:

```
locks the row
```

So:

- User A locks product
- User B must wait

👉 prevents race conditions

**Real World Example:**

You are building a ticket booking system.

Only 1 seat left.

Two users click “buy” at the same time.

Solution: transaction + row lock

Use SELECT ... FOR UPDATE inside a transaction:

```
BEGIN;

SELECT seats_available
FROM events
WHERE id = 1
FOR UPDATE;

-- application checks: seats_available > 0

UPDATE events
SET seats_available = seats_available - 1
WHERE id = 1;

INSERT INTO bookings (event_id, user_id)
VALUES (1, 123);

COMMIT;
```

## What FOR UPDATE does

It locks that event row.

So:

- User A locks event row.
- User B tries to read/update the same row and must wait.
- User A finishes and commits.
- User B then sees the updated seat count.
- If seats are now 0, User B cannot book.

### Safer single-statement update pattern

```
BEGIN;

UPDATE events
SET seats_available = seats_available - 1
WHERE id = 1
  AND seats_available > 0;

-- then check if 1 row was updated
-- if yes: insert booking
-- if no: rollback / show sold out

INSERT INTO bookings (event_id, user_id)
VALUES (1, 123);

COMMIT;
```

The key idea:

```
WHERE seats_available > 0
```

makes the update conditional.

If no row updates, there were no seats left.

## Why this matters

This is how systems like:

- banking
- Stripe
- booking systems

avoid:

```
double spending
double booking
inconsistent state
```

## Best mental model

For booking/payment/stock systems:

```
read → check → update
```

is dangerous unless protected.

Use:

```
transaction + lock
```

or:

```
atomic conditional update
```

to make the database enforce correctness.
