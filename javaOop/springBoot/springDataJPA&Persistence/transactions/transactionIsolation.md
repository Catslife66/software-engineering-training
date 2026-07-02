# Transaction Isolation

Suppose both transactions do this:

```
1. Check slot availability
2. Reserve slot
3. Commit
```

Timeline:

```
Transaction A
--------------------------
Check slot
           \
            \
             Reserve
                 \
                  Commit


Transaction B
--------------------------
Check slot
           \
            \
             Reserve
                 \
                  Commit
```

Possible Outcome

Both see:

```
Slot is available.
```

Both reserve it.

Both commit.

Database now contains:

```
Viewing A
Viewing B
```

for the same slot.

Bug

The system just double-booked.

Nothing is wrong with:

```
JPA
Hibernate
Transactions
```

The problem is:

```
Two transactions interleaved.
```

**Why Didn't Transactions Prevent This?**

This is a very common misunderstanding.

Many people think:

```
@Transactional
```

means:

```
Nobody else can touch the database.
```

It doesn't.

It only says:

```
My operations are one atomic unit.
```

It says nothing about:

```
Other transactions running simultaneously.
```

**This Is Why Isolation Exists**

Transactions have another property besides atomicity.

They also have:

```
Isolation
```

Isolation answers:

```
How much should
Transaction A
be allowed to see or interfere with
Transaction B?
```

## Lost Update

Imagine the database initially contains:

```
Viewing Slot
-------------
Saturday 10:00
Available = TRUE
```

Now two users click Book almost simultaneously.

Transaction A

```
START
      ↓
Read slot
```

Database says:

```
Available = TRUE
```

So Transaction A thinks:

```
"I can book it."
```

But it hasn't committed yet.

Transaction B

While A is still running:

```
START
      ↓
Read slot
```

What does B see?

It also sees:

```
Available = TRUE
```

because A's reservation hasn't been committed yet.

So B also thinks:

```
"I can book it."
```

Then...

Transaction A commits.

```
Slot reserved.
```

But Transaction B already made its decision.

It doesn't go back and ask:

```
"Has anything changed?"
```

It simply continues:

```
Reserve slot
Commit
```

Now you have:

```
Viewing A
Viewing B
```

for the same slot.

This Is one of the most common concurrency bugs:

**Lost Update** or more generally **Race Condition**.

## Check-then-act Race

The race condition is caused because:

```
Both transactions read the same old state before either transaction commits.
```

Suppose the booking service is:

```
@Transactional
public void bookViewing(UUID slotId) {

    Slot slot = slotRepository.findById(slotId);

    if (!slot.isAvailable()) {
        throw new SlotAlreadyBookedException();
    }

    slot.setAvailable(false);

    viewingRepository.save(...);
}
```

Two users call this method at almost exactly the same time.

Question

```
At which line does the race condition actually begin?
```

Answer:

```
Slot slot = slotRepository.findById(slotId);
```

The race condition begins here because this is where both transactions make their decision based on the same data.

So the timeline becomes:

```
Transaction A
--------------------
Read Available = true

Set Available = false
(not committed)

Transaction B
--------------------
Read Available = true
(still sees old value)
```

Because databases generally do not expose uncommitted changes to other transactions (under common isolation levels such as READ COMMITTED).

Most concurrency bugs begin at this pattern:

```
Read
    ↓
Make a decision
    ↓
Write
```

If another transaction can change the data between the read and the write, you have a race condition.

This pattern is sometimes called a **check-then-act race**.

```
Check
↓
Is the slot available?

Act
↓
Book the slot
```

If two transactions both perform the **check** before either completes the act, they can both make the same decision.

## The Three Main Solutions

When two transactions want the same row, there are three common strategies.

**1. Pessimistic Locking**

Think:

```
"I'm taking the key.
Nobody else can touch this row
until I'm finished."
```

Timeline:

```
Transaction A
-------------
Lock row
Read
Update
Commit
Unlock


Transaction B
-------------
Wait...
Wait...
Now read
```

Very safe.

But:

```
Less concurrency.
```

**2. Optimistic Locking**

Think:

```
"I'll assume nobody changes it.
If someone did,
I'll detect it later."
```

Timeline:

```
Transaction A
Read Version 1

Transaction B
Read Version 1

A commits
Version becomes 2

B tries to commit

Database says:

"No.
Version changed."
```

B retries.

Very common in Spring applications.

**3. Database Constraints**

Sometimes we simply let the database reject invalid data.

Example:

```
(property_id, viewing_time)
```

must be unique.

If two bookings try:

```
Property 5
Saturday 10:00
```

Database says:

```
Duplicate.
```

One succeeds.

One fails.

## Optimistic Locking with `@Version`

Optimistic Locking says:

```
Don't stop people reading.

Let everyone work.

But before committing,
check that nobody changed the row.
```

It is optimistic because it assumes:

```
Conflicts are rare.
```

**How Does Hibernate Know?**

It adds one extra column.

Example:

```
Slot
--------------------------
id
available
version
```

Initially:

```
id = 10
available = true
version = 1
```

**JPA**

One annotation:

```
@Version
private Long version;
```

Hibernate now manages this field automatically.

You never write:

```
slot.setVersion(...)
```

Hibernate does it.

**Timeline**

Database:

```
Slot

available = true
version = 1
```

Transaction A

Reads:

```
available = true
version = 1
```

Transaction B

Also reads:

```
available = true
version = 1
```

Still okay.

Transaction A

Updates:

```
available = false
```

Hibernate generates something very clever.

Instead of:

```
UPDATE slot
SET available = false
WHERE id = 10;
```

Hibernate generates:

```
UPDATE slot
SET available = false,
    version = 2
WHERE id = 10
AND version = 1;
```

Transaction A commits

Database now becomes:

```
available = false
version = 2
```

Transaction B

Hibernate also tries:

```
UPDATE slot
SET available = false,
    version = 2
WHERE id = 10
AND version = 1;
```

But the database now contains:

```
version = 2
```

So:

```
WHERE version = 1
```

matches:

```
ZERO ROWS
```

Hibernate immediately knows:

```
Someone else updated this row.
```

and throws:

```
OptimisticLockException
```

## Should We Use @Version Everywhere?

No.

A good engineer asks:

> How likely are conflicts?

Optimistic locking is based on the assumption:

```
Conflicts are rare.
```

### Example 1 — Good Candidate

Suppose BrightMove agents edit property descriptions.

Timeline:

```
09:00:00

Agent A opens property
```

Five minutes later:

```
09:05:00

Agent B opens same property
```

Agent A edits:

```
"Beautiful 3-bedroom house..."
```

Agent B edits:

```
"Recently renovated..."
```

Both save.

Without optimistic locking:

```
Last save wins.
```

Agent A's changes may disappear.

With `@Version`:

Agent A:

```
Version 10
↓
Save
↓
Version 11
```

Agent B:

```
Still has Version 10
↓
Save
↓
Conflict detected
```

Spring throws:

```
OptimisticLockException
```

Now the UI can say:

```
"This property was modified by another user.
Please refresh and try again."
```

### Example 2 — Bad Candidate

Imagine a counter:

```
Property Views
```

Every page refresh:

```
views++
```

Thousands of users:

```
Open property
```

at the same time.

Would optimistic locking be good?

No.

Why?

Because:

```
Conflicts happen constantly.
```

You would get:

```
OptimisticLockException
OptimisticLockException
OptimisticLockException
...
```

The system spends its time retrying.

### Engineer Rule

Use optimistic locking when:

```
conflicts are uncommon,
users edit business objects,
losing updates would be harmful.
```

Examples:

```
Property
User Profile
Booking Details
Order
```

Avoid it for:

```
View counters
Like counters
Statistics
High-frequency increments
```

Those usually use different techniques.

Rule:

```
If the entity can be updated and lost updates would matter, consider @Version.
```

## What the API should do when optimistic locking fails?

When Hibernate detects:

```
0 rows updated
```

it throws an optimistic locking exception.

In a backend API, we should usually translate that into:

```
409 CONFLICT
```

Because the request was valid, but it conflicts with the current state of the resource.

Example response idea:

```
{
  "success": false,
  "code": "VERSION_CONFLICT",
  "message": "This property was updated by someone else. Please refresh and try again."
}
```

Flow

```
Agent opens Property version 10
Agent edits price
Another user saves version 11
Agent submits old version 10
Hibernate update affects 0 rows
Optimistic lock exception
API returns 409 CONFLICT
Frontend asks user to refresh/retry
```

## Optimistic vs Pessimistic

Optimistic:

```
A reads

B reads

A commits

B discovers conflict

Retry
```

Pessimistic:

```
A locks

B waits

A commits

B continues
```

Conflict never occurs because the database prevents it.

### Real SQL

Databases support statements like:

```
SELECT *
FROM slot
WHERE id = 10
FOR UPDATE;
```

Meaning:

```
Read this row
AND
lock it.
```

Spring/JPA exposes this through:

```
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

### Which Is Faster?

Neither.

It depends.

**Optimistic**

Very good when:

```
Conflicts are rare.
```

Advantages:

```
High concurrency
Nobody waits
Excellent scalability
```

Disadvantages:

```
Sometimes retries are needed
```

**Pessimistic**

Very good when:

```
Conflicts are common.
```

Advantages:

```
No lost updates
No retries
```

Disadvantages:

```
Waiting
Lower throughput
Risk of deadlocks
```

### Engineer Thinking

Notice how we no longer ask:

_"Which annotation should I use?"_

Instead we ask:

```
How often will conflicts happen?

What is the cost of waiting?

What is the cost of retrying?

Which behaviour is better for this business?
```
