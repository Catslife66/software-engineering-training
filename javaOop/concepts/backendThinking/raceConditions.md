# Race Conditions

Scenario — Double Booking Problem

Two users try to book the LAST viewing slot simultaneously.

Example: Available slots = 1

At the same moment:

```
User A books
User B books
```

Without protection, this can happen:

```
A reads slots = 1
B reads slots = 1

A decrements to 0
B decrements to 0

Result:
2 bookings
1 slot
```

This is called: **Race Condition**

## 1. Why does this happen?

This happens because two concurrent requests read the same available slot value before either transaction finishes updating it.
Both requests believe the slot is still available, which causes overselling/double booking.

## 2. Why is transaction alone NOT enough here?

Transactions guarantee atomicity inside one transaction, but they do not automatically prevent two transactions from reading the same data concurrently.

Example:

```
Transaction A reads slots = 1
Transaction B reads slots = 1
```

Both transactions are individually valid.

Without locking/isolation/atomic update protections, both may still commit.

So the key problem is:

```
concurrent access to shared state
```

This is an extremely important backend concept.

## 3. What are possible solutions?

**Locking**

```
SELECT ... FOR UPDATE
```

**Atomic update**

```
UPDATE slots
SET available = available - 1
WHERE id = ?
AND available > 0
```

Then check: rows affected == 1

If 0 rows updated: slot already sold out

Database does check + update in one atomic operation.

This dramatically reduces concurrency problems and avoids race conditions very efficiently.

```
avoids race conditions
avoids holding locks for long periods

which means:

higher throughput
higher concurrency
less waiting
```

**Queue**

Queues serialize processing during high traffic spikes.

Common in:

- ticket sales
- flash sales
- payment processing

**DB constraints**

Database constraints are often the final protection layer.

Example:

```
UNIQUE constraints
CHECK constraints
```

## 4. Which layer should protect against this?

```
Service layer coordinates workflow
Database/repository layer enforces concurrency safety
```

Examples:

- row locks
- atomic updates
- constraints
- transaction isolation

Why service layer alone is NOT enough?

Because concurrency happens at the data level and two service instances can still run simultaneously.

The final source of truth is: database consistency guarantees

## Deadlocks

Scenario

Imagine two transactions.

```
Transaction A
lock User row
then lock Property row

Transaction B
lock Property row
then lock User row
```

Now:

```
Transaction A has User lock
Transaction B has Property lock
```

Then:

```
Transaction A waits for Property
Transaction B waits for User
```

Result:

```
A waits for B
B waits for A
```

Nobody can continue.

This is called **deadlock**

### What happens when database detects a deadlock?

Most databases detect deadlocks automatically.

```
database chooses a deadlock victim
rolls it back
returns an error
application may retry
```

Example:

```
Transaction A succeeds
Transaction B rolled back
```

Then the application can retry B.

### How can we reduce deadlocks?

**Keep transactions short**

Bad:

```
lock row
call external API
send email
wait 5 seconds
commit
```

Good:

```
lock row
update row
commit
```

Short transactions reduce deadlock opportunities.

**Lock resources in a consistent order**

Everywhere in the system:

```
User
→ Property
```

always.

Transaction A:

```
lock User
lock Property
```

Transaction B:

```
lock User
lock Property
```

Both follow the same order.

Result:

```
one waits
other proceeds
```

No circular waiting.

One of the most powerful deadlock prevention techniques is:

```
Always acquire locks in the same order.
```

Many large systems use this rule.

Example

Money transfer:

Bad:

```
Transfer A→B
lock A then B

Transfer B→A
lock B then A
```

Deadlock risk.

Better:

```
Always lock lower account ID first.
```

Example:

```
lock Account 100
then Account 200
```

everywhere.

Deadlock disappears.
