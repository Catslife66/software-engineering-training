# Concurrency & Race Conditions

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

### Locking

```
SELECT ... FOR UPDATE
```

locks the row until transaction completes.

1. Pessimistic Locking

> “Assume conflicts WILL happen.”

Example:

```
SELECT * FROM slots
WHERE id = 123
FOR UPDATE;
```

This locks the row.

Other transactions must wait.

2. Optimistic Locking

> “Assume conflicts are rare.”

Instead of locking immediately:

- read data normally
- update only if version still matches

Example: slot.version = 5

Update query:

```
UPDATE slots
SET available = 0,
    version = version + 1
WHERE id = 123
AND version = 5;
```

If another transaction already updated: 0 rows updated

meaning: conflict detected

3. CHECKPOINT

- Which locking strategy is better for:

  For high-conflict booking, use pessimistic locking or atomic update. Atomic update is often more scalable because it avoids holding long locks.

  User editing takes a long time. If we used pessimistic locking, one user could lock the property record for several minutes while editing, blocking everyone else. Optimistic locking allows multiple users to edit freely, and only detects conflict when they save.

- What should backend return when optimistic locking detects conflict?

  return 409 Conflict

  Then frontend can say:

  This property was updated by someone else. Please refresh and review the latest version.

  The user may then:
  - reload latest data
  - compare changes
  - reapply their edits

  Do not silently overwrite newer data.

- Where is version checking usually enforced?

  In Spring/JPA, this is often handled using a @Version field, but conceptually the database enforces the check.

4. Mental Model

**Pessimistic locking**:

```
Lock first
→ guarantee exclusivity
→ reduce concurrency
```

Good for:

- ticket booking
- inventory
- payments
- high-conflict resources

**Optimistic locking:**

```
Allow concurrent work
→ detect conflict later
→ maximize concurrency
```

Good for:

- dashboards
- profile editing
- CMS/admin systems
- collaborative editing (light conflict)

| Strategy            | Idea                  | Tradeoff                         |
| ------------------- | --------------------- | -------------------------------- |
| Atomic update       | one-step DB operation | scalable but limited flexibility |
| Pessimistic locking | block others early    | safer but slower                 |
| Optimistic locking  | detect conflict later | scalable but conflicts possible  |

### Atomic update

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

### Queue

Queues serialize processing during high traffic spikes.

Common in:

- ticket sales
- flash sales
- payment processing

### DB constraints

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
