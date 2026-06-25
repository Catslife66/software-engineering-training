# @Transactional

Imagine:

```
Book Viewing
```

Business workflow:

```
1. Create Viewing
2. Reserve Time Slot
3. Send Confirmation Email
```

The Product Manager sees:

```
One action:
"Book Viewing"
```

But the database sees:

```
Multiple operations
```

The Problem

Suppose:

```
1. Create Viewing      ✅
2. Reserve Slot        ✅
3. Send Confirmation   ❌
```

What should happen?

Option A:

```
Keep steps 1 and 2.
```

Option B:

```
Undo steps 1 and 2.
```

This is exactly what transactions solve.

## What is a Transaction?

Think:

```
A transaction is a unit of work.
```

Example:

```
Create User
Create Subscription
Create Payment Record
```

might be:

```
One transaction
```

## ACID

Transactions exist because databases provide ACID guarantees.

We'll go deep into ACID later.

For now, remember:

```
All operations succeed
OR
Nothing succeeds
```

This is called:

```
Atomicity
```

## Spring Version

Service:

```
@Service
public class ViewingService {

    @Transactional
    public void bookViewing(...) {
        viewingRepository.save(viewing);
        slotRepository.reserve(slot);
        notificationService.sendEmail(...);
    }
}
```

Conceptually:

```
Start Transaction
        ↓
Execute Method
        ↓
Success?
```

If yes:

```
COMMIT
```

If no:

```
ROLLBACK
```

## Commit

Commit means:

```
Make changes permanent.
```

Before commit:

```
Changes are still inside transaction.
```

After commit:

```
Changes are stored in database.
```

## Rollback

Rollback means:

```
Undo everything in this transaction.
```

Example:

```
Create Viewing      ✅
Reserve Slot        ✅
Exception           ❌
```

Rollback:

```
Viewing removed
Slot reservation removed
```

Database returns to original state.

## Why This Matters For Hibernate

Remember:

```
Managed Entity
       ↓
Setter called
       ↓
Dirty Checking notices change
       ↓
Flush
       ↓
SQL generated
       ↓
COMMIT
       ↓
Transaction ends
       ↓
Persistence Context destroyed
       ↓
Entities become Detached
```

### Flush

Think:

```
Persistence Context
        ↓
Flush
        ↓
Database
```

Flush means:

```
Synchronize Hibernate state with database state.
```

Hibernate generates SQL, for example:

```
UPDATE users
SET email = 'new@email.com'
WHERE id = ...
```

and sends it to the database.

At this point the transaction is still open.

Think:

```
SQL executed
but transaction not committed yet
```

### Example

Inside transaction:

```
@Transactional
public void updateUserEmail() {

    User user = userRepository.findById(id);

    user.setEmail("new@email.com");
}
```

Question:

When you call:

```
user.setEmail(...)
```

does Hibernate immediately execute:

```
UPDATE users ...
```

?

No.

Instead:

```
Persistence Context updated
        ↓
Dirty Checking
        ↓
Flush later
```

### Why This Matters

Suppose:

```
@Transactional
public void updateUser() {

    user.setEmail(...);

    throw new RuntimeException();
}
```

If Hibernate updated immediately:

```
Email changed forever
```

which would be bad.

Instead:

```
Change stays inside transaction
        ↓
Exception occurs
        ↓
Rollback
        ↓
Database unchanged
```

This is one of the reasons transactions are so powerful.

Step 1

```
User user = userRepository.findById(id);
```

Result:

```
User becomes Managed
```

and is stored in:

```
Persistence Context
```

Step 2

```
user.setEmail("new@email.com");
```

Result:

```
Managed entity changed
```

Hibernate notices:

```
Original email
      ↓
New email
```

through:

```
Dirty Checking
```

Step 3

```
throw new RuntimeException();
```

Result:

```
Transaction marked for rollback
```

Step 4

Spring sees:

```
Unchecked exception
```

and performs:

```
ROLLBACK
```

instead of:

```
COMMIT
```

Final Database State

```
Original email remains
```

because:

```
No successful commit occurred.
```

### Complete Life Circle

```
Load User
     ↓
Managed Entity
     ↓
Setter called
     ↓
Dirty Checking
     ↓
Flush
     ↓
UPDATE SQL generated
     ↓
Commit
     ↓
Changes permanent
     ↓
Persistence Context destroyed
     ↓
Detached Entity
```

## External side effects

Remember Phase 2 We discussed:

```
Outbox Pattern
```

Now you can see why it exists.

Example:

```
@Transactional
public void registerUser(...) {

    userRepository.save(user);

    emailService.sendWelcomeEmail();

    throw new RuntimeException();
}
```

The outcome:

```
User rolled back
Email sent
```

Now the system has become inconsistent.

Instead of:

```
sendEmail();
```

inside the transaction:

```
saveViewing();
saveOutboxMessage();
```

Commit transaction.

Then later:

```
Background worker
     ↓
Reads outbox
     ↓
Sends email
```

This guarantees:

```
Database committed first
Then email sent
```

**Very Important Engineering Insight**

Transactions guarantee:

```
Database consistency
```

They do NOT automatically guarantee:

```
Email consistency
Kafka consistency
Payment gateway consistency
External API consistency
```

That's why reliability patterns exist.

A transaction protects:

```
Local consistency
```

inside one database.

It does NOT automatically protect:

```
Distributed consistency
```

across:

```
Database
+
Email Service
+
Payment Provider
+
Message Queue
```

That requires additional patterns.

## Transaction Boundary

Think:

```
Transaction Boundary
```

means:

```
Where does the transaction start?
Where does the transaction end?
```

For most applications:

```
Controller
      ↓
Service  ← transaction boundary
      ↓
Repository
```

## What causes a rollback?

Example

```
@Transactional
public void registerUser() {

    userRepository.save(user);

    throw new RuntimeException();
}
```

Result:

```
Rollback
```

Because:

```
RuntimeException is an Unchecked Exception
```

### Spring's Default Rule

By default Spring rolls back on:

```
RuntimeException
Error
```

Examples:

```
throw new RuntimeException();

throw new IllegalArgumentException();

throw new NullPointerException();
```

All trigger rollback.

### Checked Exceptions

Now look at:

```
@Transactional
public void registerUser()
        throws IOException {

    userRepository.save(user);

    throw new IOException();
}
```

Question:

```
Will Spring roll back?
```

Surprisingly:

```
No (by default)
```

because:

```
IOException is a Checked Exception
```

**Why?**

Historically Spring assumes:

```
RuntimeException
    ↓
Unexpected system failure

Checked Exception
    ↓
Business/application condition
```

So default behavior is:

```
Unchecked Exception
    ↓
Rollback

Checked Exception
    ↓
Commit
```

unless configured otherwise.

## Explicit Rollback

If you want:

```
IOException
```

to trigger rollback:

```
@Transactional(
    rollbackFor = IOException.class
)
```

Now:

```
IOException
    ↓
Rollback
```

## Many Teams Do One Of Two Things

**Option 1**

Convert business failures into:

```
RuntimeException
```

Example:

```
throw new PropertyCreationException(...);
```

where:

```
public class PropertyCreationException
        extends RuntimeException {
}
```

Then rollback happens automatically.

**Option 2**

Explicitly configure rollback:

```
@Transactional(
    rollbackFor = IOException.class
)
```

Now:

```
IOException
    ↓
Rollback
```

## Current Transaction Rules

| Exception Type           | Default Spring Behavior |
| ------------------------ | ----------------------- |
| RuntimeException         | Rollback                |
| NullPointerException     | Rollback                |
| IllegalArgumentException | Rollback                |
| IllegalStateException    | Rollback                |
| Error                    | Rollback                |
| IOException              | Commit                  |
| SQLException             | Commit                  |
| Checked Exception        | Commit                  |

unless:

```
@Transactional(
    rollbackFor = ...
)
```

is configured.

## A Very Important Real-World Bug

```
@Transactional
public void registerUser() {

    try {

        userRepository.save(user);

        throw new RuntimeException();

    } catch (Exception e) {

        System.out.println("Error");

    }
}
```

`RuntimeException` is an `Exception`, so it is caught.

So commit.

Because Spring only rolls back automatically when the exception escapes the transactional method.

Here the exception happens:

```
throw new RuntimeException();
```

but then it is swallowed:

```
catch (Exception e) {
    System.out.println("Error");
}
```

So from Spring’s point of view, the method finished normally.

Spring thinks:

```
No exception left the method
→ commit transaction
```

Even though something failed.

**Rule**

```
Caught exception = Spring cannot see it
Escaped RuntimeException = Spring rolls back
```

To force rollback, either rethrow:

```
catch (Exception e) {
    throw e;
}
```

or throw a new runtime exception:

```
catch (Exception e) {
    throw new RuntimeException(e);
}
```

## Very Important Engineer Rule

Many production bugs come from this:

❌ Bad:

```
catch (Exception e) {
    log.error("Failed");
}
```

because:

```
Exception swallowed
Transaction commits
```

✅ Better:

```
catch (Exception e) {
    log.error("Failed");
    throw e;
}
```

or:

```
catch (Exception e) {
    throw new RuntimeException(e);
}
```

because:

```
Exception escapes
Spring rolls back
```
