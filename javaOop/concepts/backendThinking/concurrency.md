# Concurrency & Reliability

Scenario

Property editing system.

Two agents open the same property edit page.

Both see:

```
title = "Nice Flat"
version = 3
```

Agent A changes title and saves first.

Database becomes:

```
title = "Modern Flat"
version = 4
```

Then Agent B clicks save using stale version 3.

Without protection:

```
A's changes get overwritten
```

This is called **Lost Update Problem**

## Pessimistic Locking VS Optimistic Locking

1. Pessimistic Locking

> “Assume conflicts WILL happen.”

```
lock row
read
calculate
update
commit
unlock
```

Many steps.

_atomic update check + update in one operation -> Much faster under heavy load._

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

  A High-conflict ticket booking system

  B Property editing dashboard

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

## Mental Model

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

Suppose the team agrees:

```
Always lock User first
Then Property
```

Now:

```
Transaction A
lock User
lock Property

Transaction B
lock User
lock Property
```

What happens?

```
A gets User lock

B tries User lock
B waits
```

Then:

```
A gets Property
A finishes
A releases locks

B continues
```

No deadlock.

Why?

Because:

```
waiting is allowed
circular waiting is not
```

Lock ordering removes the possibility of a cycle.

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
