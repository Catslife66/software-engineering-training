# Transactions & Concurrency

A transaction defines a **unit of work** that should succeed or fail as one logical operation.

BrightMove examples:

```
Book viewing
→ check property
→ check slot
→ create viewing
→ update availability
```

or:

```
Transfer property to another agent
→ remove old ownership
→ assign new ownership
→ write audit record
```

If one step fails halfway through, we usually do not want a partially completed workflow.

This module brings together:

- @Transactional
- transaction boundaries
- atomicity
- rollback
- read-only transactions
- propagation
- isolation
- race conditions
- optimistic locking
- pessimistic locking
- multi-repository workflows
- idempotency and retry safety
- outbox implementation

The central engineering question is:

**Which operations must succeed or fail together, and how do we protect that workflow when multiple requests run concurrently?**

## Transaction

Suppose booking a viewing requires:

```
1. Find property
2. Verify it is available
3. Check viewing slot
4. Create viewing
5. Save booking
```

Without a transaction:

```
Step 1 ✓
Step 2 ✓
Step 3 ✓
Step 4 ✓
Step 5 ✗
```

The system may be left in an inconsistent state.

A transaction gives us:

```
BEGIN
  Step 1
  Step 2
  Step 3
  Step 4
  Step 5
COMMIT
```

If the workflow fails:

```
BEGIN
  Step 1
  Step 2
  Step 3
  failure
ROLLBACK
```

The database returns to the previous committed state.

## Transaction Boundary

The most important design decision is not:

Where can I put `@Transactional`?

It is:

**What is the business unit that must be atomic?**

For example:

```
@Transactional
public ViewingResponse createViewing(...) {
    ...
}
```

The transaction boundary should normally surround the business workflow, not an arbitrary individual repository call.

A useful architecture is:

```
Controller
    ↓
Service method
    ↓
@Transactional boundary begins
    ↓
Business workflow
    ↓
Repositories
    ↓
@Transactional boundary ends
```

So transactions usually belong at the service layer.

## `@Transactional`

Basic example:

```
@Transactional
public PropertyResponse updateProperty(
        UUID id,
        UpdatePropertyRequest request
) {
    Property property = propertyRepository.findById(id)
            .orElseThrow(
                    () -> new PropertyNotFoundException(id)
            );

    property.setTitle(request.title());
    property.setPrice(request.price());

    return propertyMapper.toResponse(property);
}
```

Conceptually:

```
Enter method
    ↓
Spring opens transaction
    ↓
Hibernate persistence context participates
    ↓
method executes
    ↓
dirty checking / flush
    ↓
commit
```

If an appropriate exception escapes:

```
exception
    ↓
transaction rollback
```

## How Spring Applies @Transactional

`@Transactional` is not magic built into the Java language.

Spring commonly wraps the service bean in a proxy.

Conceptually:

```
Caller
   ↓
Spring Transaction Proxy
   ↓
begin transaction
   ↓
PropertyService.updateProperty()
   ↓
commit / rollback
```

The proxy manages transaction behaviour around the method.

This has an important consequence.

If one method inside the same class directly calls another `@Transactional` method on this, that call may bypass the proxy and therefore not apply a new transactional behaviour as expected.

Example:

```
public void outer() {
    inner();
}

@Transactional
public void inner() {
}
```

A direct self-call can be problematic because:

```
outer()
→ this.inner()
```

does not necessarily go back through the Spring proxy.

For ordinary service design, avoid relying on self-invocation to change transaction semantics.

## Atomicity and Rollback

Atomicity means:

Either the transaction succeeds as one unit, or none of its changes become committed.

Suppose:

```
@Transactional
public void transferProperty(
        UUID propertyId,
        UUID newAgentId
) {
    Property property = ...;
    Agent newAgent = ...;

    property.assignTo(newAgent);

    auditRepository.save(
            new OwnershipAudit(...)
    );

    notificationService.send(...);
}
```

The database changes involving:

```
Property
Audit
```

can participate in one transaction.

But an external email or HTTP call does not automatically roll back just because the database does.

This distinction becomes critical later in the outbox section.

## Default Rollback Behaviour

By default, Spring rolls back on unchecked exceptions such as:

```
RuntimeException
```

and its subclasses.

For example:

```
public class BookingConflictException
        extends RuntimeException {
}
```

If this escapes a transactional method:

```
BookingConflictException
    ↓
Spring marks transaction for rollback
```

Checked exceptions do not automatically trigger rollback under the default rules.

If you explicitly need rollback for a checked exception:

```
@Transactional(rollbackFor = SomeCheckedException.class)
```

can configure that behaviour.

A practical rule is:

Application/business exceptions in Spring are commonly modelled as unchecked exceptions, but rollback semantics should be chosen deliberately rather than memorized mechanically.

## Read-Only Transactions

For read workflows:

```
@Transactional(readOnly = true)
public PropertyResponse getPropertyById(UUID id) {
    ...
}
```

This communicates:

This transaction is intended only for reading.

Benefits include:

- documenting intent
- allowing framework/provider optimizations
- reducing accidental write expectations

Use:

```
@Transactional
```

for writes.

Use:

```
@Transactional(readOnly = true)
```

for read-only service workflows when a transaction/persistence context is useful.

Do not treat `readOnly = true` as an absolute security mechanism preventing every possible write. It is primarily a transactional hint and design declaration.

## Multi-Repository Workflows

Transactions become especially important when one service operation touches multiple repositories.

Example:

```
@Transactional
public void approveViewing(UUID viewingId) {

    Viewing viewing = viewingRepository.findById(viewingId)
            .orElseThrow(
                    () -> new ViewingNotFoundException(viewingId)
            );

    viewing.approve();

    auditRepository.save(
            new ViewingAudit(
                    viewingId,
                    "APPROVED"
            )
    );
}
```

We want:

```
Viewing update
+
Audit insert
```

to succeed together.

Without one service transaction, we risk:

```
Viewing committed
Audit failed
```

and inconsistent business history.

This is why repository methods alone are not the right transaction boundary for complex workflows.

## Transaction Propagation

Propagation decides whether a method joins an existing transaction or starts a different one.

The default is:

```
Propagation.REQUIRED
```

Meaning:

**Join the current transaction if one exists; otherwise create a new one.**

Example:

```
@Transactional
public void outerWorkflow() {
    innerWorkflow();
}
```

If `innerWorkflow()` participates through a properly proxied Spring bean and also uses the default transaction propagation:

```
outer transaction
      ↓
inner joins same transaction
```

Both succeed or fail together.

---

`REQUIRED`

Most common.

```
@Transactional(
    propagation = Propagation.REQUIRED
)
```

Meaning:

```
If a transaction already exists, join it.
If no transaction exists, start one.
```

So in this code:

```
@Transactional
public void createProperty() {
    propertyRepository.save(property);
    auditService.recordCreation(property);
}
```

and:

```
@Transactional
public void recordCreation(...) {
    auditRepository.save(audit);
}
```

by default, recordCreation() joins the existing transaction.

So default behavior is:

```
one shared transaction
```

Big picture:

```
One business action

One transaction

Everything succeeds or fails together.
```

---

`REQUIRES_NEW`

```
@Transactional(
    propagation = Propagation.REQUIRES_NEW
)
```

Meaning:

Suspend the current transaction and create a completely separate one.

Example:

```
@Transactional
public void createProperty() {

    propertyRepository.save(property);
    auditService.recordCreation();

}
@Transactional(propagation = REQUIRES_NEW)
public void recordCreation() {

    auditRepository.save(...);

}
```

Conceptually:

```
Transaction A
    ↓ suspended

Transaction B
    ↓
commit/rollback independently

Transaction A resumes
```

This is useful in specialized cases, such as certain audit operations, but should not be used casually.

A dangerous assumption is:

“If I use REQUIRES_NEW, my logging is always safe.”

It changes consistency semantics, so it must match the business requirement.

Big picture:

```
Nested business action

Independent transaction

Failure isolation.
```

Problem:

REQUIRES_NEW makes the audit/analytics transaction independent,
but if it throws an exception and you don’t catch it,
that exception can still escape back to PropertyService and cause the property transaction to roll back.

So REQUIRES_NEW alone does not protect the main workflow.

## Isolation

Transactions can overlap.

Isolation determines how much one transaction can observe the intermediate effects of another.

The standard isolation levels include:

```
READ_UNCOMMITTED
can see uncommitted data
- dirty reads allowed

READ_COMMITTED (most common)
only see committed data
- phantom reads possible

REPEATABLE_READ
same rows stay consistent
- phantom reads still possible

SERIALIZABLE (strongest)
as if transactions run one by one
- no anomalies
- but slower
```

We do not need to memorize database implementation details here.

The important conceptual progression is:

```
lower isolation
→ more concurrency
→ more anomalies possible

higher isolation
→ stronger isolation
→ potentially lower concurrency / more locking
```

Typical anomalies include: (see transaction in SQL course)

- dirty reads
- non-repeatable reads
- phantom reads
- lost-update-style races

Spring can specify:

```
@Transactional(
    isolation = Isolation.READ_COMMITTED
)
```

but the effective behaviour also depends on the database.

The normal default is generally inherited from the database

## Why Transactions Alone Do Not Prevent Race Conditions

This is one of the most important lessons in this module.

Suppose a viewing slot is currently available.

Transaction A:

```
read slot → AVAILABLE
```

Transaction B:

```
read slot → AVAILABLE
```

Both transactions are individually valid and isolated.

Then both decide:

```
book slot
```

If nothing else protects the invariant, we may get double booking.

So:

**A transaction gives atomicity, but it does not automatically serialize every business decision.**

Concurrency control is a separate design problem.

## Race Condition Example

Imagine:

```
Slot: 10:00
status = AVAILABLE
```

Two requests arrive:

```
Transaction A          Transaction B

read AVAILABLE         read AVAILABLE

decide book            decide book

write BOOKED           write BOOKED
```

Both decisions were based on stale shared state.

This is a race condition.

The solution depends on the type of business conflict.

Common tools are:

```
Optimistic locking
Pessimistic locking
Database unique constraints
Atomic SQL operations
```

## Optimistic Locking

Optimistic locking assumes:

Conflicts are possible, but relatively uncommon.

We already mapped:

```
@Version
private Long version;
```

Suppose:

```
Property
version = 7
```

Transaction A loads version 7.

Transaction B loads version 7.

A commits first:

```
version 7 → 8
```

B later attempts an update expecting version 7.

Hibernate effectively uses a condition like:

```
UPDATE properties
SET price = ?,
    version = 8
WHERE id = ?
  AND version = 7;
```

But the database now has:

```
version = 8
```

So:

```
0 rows affected
```

Hibernate detects the conflict.

Spring may expose this through an exception such as:

```
ObjectOptimisticLockingFailureException
```

which can be mapped to:

```
409 Conflict
```

## Why Optimistic Locking Works

The invariant is:

Update only if nobody changed this entity since I read it.

Conceptually:

```
Read version 7
      ↓
Do work
      ↓
UPDATE ... WHERE version = 7
      ↓
1 row?
   ├── yes → success
   └── no  → conflict
```

No long database lock is held during the user's entire editing process.

That makes optimistic locking appropriate when:

- collisions are relatively rare
- lost updates matter
- retries/reloads are acceptable

Examples:

```
property edits
profile edits
document metadata
```

## Stale Client vs Concurrent Transaction

There are two related scenarios.

**Stale client**

Client read version 4 yesterday.

Database is now version 6.

Client submits version 4.

The service can compare:

```
request version = 4
database version = 6
```

and reject immediately.

---

**Concurrent update during current transaction**

Even if versions match when checked:

```
request = 6
database = 6
```

another transaction could commit version 7 before ours writes.

Hibernate's @Version condition catches that race at update time.

So the two protections are conceptually:

```
Service version check
→ detects already-stale client

Hibernate @Version
→ protects the actual concurrent database update
```

## Pessimistic Locking

Pessimistic locking assumes:

Conflict is likely or too dangerous to allow competing transactions to proceed independently.

Instead of detecting conflict later, we lock the row while working.

A Spring Data JPA repository can use something like:

```
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT v
    FROM ViewingSlot v
    WHERE v.id = :id
""")
Optional<ViewingSlot> findByIdForUpdate(
        @Param("id") UUID id
);
```

Conceptually:

```
Transaction A
    ↓
locks slot row
    ↓
Transaction B tries same row
    ↓
must wait / fail depending on DB behaviour
```

This can be useful for scarce resources where concurrent booking is common and double booking is unacceptable.

Examples:

```
inventory
seat booking
high-contention appointment slot
limited stock
```

## Optimistic vs Pessimistic

The choice is a trade-off.

| Optimistic                | Pessimistic                      |
| ------------------------- | -------------------------------- |
| Detect conflict later     | Prevent competing access earlier |
| No long row lock          | Holds DB lock                    |
| Better for rare conflicts | Better for high contention       |
| Conflict → retry/reload   | Other transactions may wait      |
| Higher concurrency        | Stronger immediate control       |

A practical BrightMove distinction:

```
Property description/price editing
→ optimistic locking often suitable

Highly contested viewing slot
→ pessimistic locking or DB constraint may be more suitable
```

The business invariant should determine the strategy.

## Database Constraints Still Matter

Application-level checks are not enough for some invariants.

Suppose one viewing slot must be unique for:

```
property_id + requested_date_time
```

A database unique constraint can provide final protection:

```
UNIQUE(property_id, requested_date_time)
```

Why?

Because two transactions can both pass this application check:

```
if (!viewingRepository.existsByPropertyIdAndTime(...)) {
    ...
}
```

before either inserts.

A unique constraint lets the database enforce:

There can never be two committed rows with this key.

This gives us a strong engineering principle:

**Critical data invariants should often have database protection, not only application checks.**

## Retry Safety

Concurrency failures and temporary infrastructure failures sometimes lead to retries.

But retrying blindly can be dangerous.

Imagine:

```
Request
→ charge customer
→ timeout
```

The client doesn't know whether the charge succeeded.

If it simply retries:

```
charge again
```

the customer may be charged twice.

Retry safety asks:

Can this operation be executed again without causing unintended duplicate effects?

This leads directly to idempotency.

## Idempotency

An operation is idempotent when repeating the same request produces the same intended effect rather than duplicating it.

For example:

```
Set status = CANCELLED
```

can often be idempotent.

Doing it twice still leaves:

```
CANCELLED
```

But:

```
Create new payment
```

is not naturally idempotent.

A common API pattern is an idempotency key:

```
Idempotency-Key: abc-123
```

The server records:

```
abc-123
→ already processed
```

A retry with the same key returns/reuses the previous outcome instead of repeating the side effect.

Example:

Idempotency in BrightMove

Suppose:

```
POST /viewings
```

is retried because the client timed out.

Without protection:

```
Viewing A created
client didn't receive response
client retries
Viewing B created
```

Now there are duplicates.

With a request key:

```
request key = XYZ
```

the server can detect:

```
XYZ already processed
```

and avoid creating a second viewing.

This is especially important in distributed systems where:

```
timeout
```

does not tell us whether:

```
the server failed
```

or:

```
the server succeeded but the response was lost
```

## Synchronous Side Effects Problem

Consider:

```
@Transactional
public void createViewing(...) {

    viewingRepository.save(viewing);

    emailService.sendConfirmation(viewing);
}
```

This looks reasonable, but there is a failure problem.

Scenario:

```
Database insert succeeds
Email sends successfully
Transaction later fails
Database rolls back
```

The user received confirmation for a booking that does not exist.

Or:

```
Database work succeeds
Email service fails
```

Should the entire booking roll back just because email is unavailable?

Usually not.

External side effects don't share the database transaction.

This is where the outbox pattern becomes useful.

## Outbox Pattern

The outbox pattern solves:

How do I atomically record a database change and the fact that an external event should later be published?

Instead of:

```
Transaction
├── save Viewing
└── send message directly
```

we do:

```
ONE database transaction

├── save Viewing
└── save OutboxEvent
```

Both rows commit together.

Example:

```
viewings
---------
V123


outbox_events
-------------
VIEWING_CREATED | V123 | PENDING
```

Then a separate publisher later reads:

```
PENDING outbox events
```

and sends messages/emails/events.

### BrightMove Outbox Example

Transactional service:

```
@Transactional
public ViewingResponse createViewing(...) {

    Viewing viewing = ...;

    viewingRepository.save(viewing);

    OutboxEvent event = new OutboxEvent(
            "VIEWING_CREATED",
            viewing.getId().toString(),
            ...
    );

    outboxRepository.save(event);

    return mapper.toResponse(viewing);
}
```

Now:

```
Viewing insert
+
Outbox insert
```

are one database transaction.

If either fails:

```
ROLLBACK BOTH
```

If both commit:

```
eventual publisher can safely process event
```

### Why Outbox Is Better Than Publishing Directly

Without outbox:

```
save DB
    ↓
publish event
```

there is always a dangerous gap.

Failure after database commit but before publish:

```
data exists
event missing
```

Failure after publish but before database commit:

```
event exists
data rolled back
```

With outbox:

```
Business data
+
event intent
```

are stored atomically in the same database transaction.

Then external delivery becomes **eventually consistent**.

### Outbox Does Not Automatically Guarantee Exactly-Once Delivery

This is important.

The outbox publisher may:

```
send event
crash before marking it SENT
```

Then it may send the event again after restart.

Therefore consumers should often be idempotent.

So:

```
Outbox
→ reliable event recording

Idempotent consumer
→ safe duplicate processing
```

These concepts work together.

## Transaction + Outbox + Idempotency

This creates a strong reliability chain:

```
Incoming request
    ↓
Idempotency check
    ↓
@Transactional business workflow
    ↓
Business data + Outbox row
    ↓
COMMIT
    ↓
Outbox publisher
    ↓
External consumer
    ↓
Consumer idempotency
```

This is the same reliability thinking we developed conceptually earlier, now expressed in Spring/JPA terms.

## Transaction Boundaries and External Calls

A transaction should usually protect:

```
database state
```

It should not be stretched across slow external calls unnecessarily.

Avoid:

```
@Transactional
public void workflow() {

    databaseWork();

    externalApiCall(); // may take seconds

    moreDatabaseWork();
}
```

Why?

The transaction may hold:

- database connections
- locks
- managed state

for the entire external call duration.

That increases contention and failure complexity.

A cleaner design is often:

```
transaction
→ commit business state + outbox intent
→ perform external work asynchronously
```

## Transaction Scope Should Be Small but Complete

These two goals must be balanced.

Too small:

```
repository call transaction
repository call transaction
repository call transaction
```

can break atomicity across the workflow.

Too large:

```
open transaction
→ database work
→ network calls
→ file upload
→ email
→ long CPU task
→ commit
```

creates unnecessary contention.

The right boundary is:

**The smallest boundary that fully protects the database consistency requirements of the business operation.**

## Request & Data Flow

Booking Example

Suppose:

```
POST /properties/{id}/viewings
```

Service:

```
@Transactional
public ViewingResponse createViewing(
        UUID propertyId,
        CreateViewingRequest request
) {
    Property property = propertyRepository.findById(propertyId)
            .orElseThrow(
                    () -> new PropertyNotFoundException(propertyId)
            );

    boolean exists =
            viewingRepository.existsByPropertyIdAndRequestedDateTime(
                    propertyId,
                    request.requestedDateTime()
            );

    if (exists) {
        throw new BookingConflictException();
    }

    Viewing viewing = new Viewing(
            request.requestedDateTime()
    );

    property.addViewing(viewing);

    outboxRepository.save(
            OutboxEvent.viewingCreated(viewing)
    );

    return viewingMapper.toResponse(viewing);
}
```

Conceptual flow:

```
HTTP Request
    ↓
Controller
    ↓
@Transactional Service
    ↓
BEGIN
    ↓
Load property
    ↓
Check slot
    ↓
Create viewing
    ↓
Save business state
    ↓
Save outbox event
    ↓
Flush
    ↓
COMMIT
    ↓
HTTP response
```

Later:

```
Outbox publisher
    ↓
send confirmation/event
```

## Common Mistakes

**Putting @Transactional on controllers**

Avoid making HTTP handling responsible for database atomicity.

Prefer service-layer transaction boundaries.

---

**Assuming transaction = concurrency protection**

A transaction does not automatically prevent two requests from reading the same available state and making conflicting decisions.

Concurrency requires additional mechanisms.

---

**Using optimistic locking for every problem**

Optimistic locking is useful for stale updates.

It may not be sufficient for highly contested resource allocation.

---

**Using pessimistic locking everywhere**

Locks reduce concurrency and can create:

- waiting
- deadlocks
- throughput problems

Use them where contention/business risk justifies them.

---

**Depending only on exists() before insert**

This is vulnerable to races.

For critical uniqueness, use database constraints as final enforcement.

---

**Retrying non-idempotent workflows blindly**

Retries can duplicate:

```
payments
bookings
messages
orders
```

Design idempotency before automatic retries.

---

**Publishing external events inside the database transaction**

External side effects cannot be rolled back with PostgreSQL.

Use outbox/eventual consistency when appropriate.

---

**Making transactions too large**

Long transactions increase:

- lock duration
- connection usage
- conflict probability
- operational risk

## Engineering Trade-offs

Transactions give strong local database consistency.

But stronger consistency and stronger locking generally reduce concurrency.

This creates a recurring systems trade-off:

```
Consistency guarantees
        ↕
Concurrency / throughput
```

Optimistic locking favours concurrency and handles conflicts after they occur.

Pessimistic locking favours immediate exclusion but reduces concurrent access.

Outbox trades immediate external consistency for reliable eventual consistency.

Idempotency adds storage/logic complexity but makes retries safe.

There is no single universal strategy.

Choose based on:

- how common conflicts are
- how costly duplicate actions are
- whether external systems are involved
- how much latency is acceptable
- which invariants must never be broken

## The Transaction Design Framework

For every business workflow, ask these questions in order:

```
1. What is the business operation?

2. Which database changes must succeed together?

3. Where should the transaction begin and end?

4. What can fail?

5. What concurrency conflicts are possible?

6. Which invariants need DB constraints?

7. Optimistic or pessimistic locking?

8. Can the operation be retried safely?

9. Are there external side effects?

10. Should they use an outbox/eventual consistency?
```

This framework is much more important than memorizing every @Transactional option.

## Summary

A transaction represents:

**One atomic unit of database work**

The normal Spring architecture is:

```
Controller
    ↓
@Transactional Service
    ↓
Repositories
    ↓
Database
```

Important concepts:

```
@Transactional
→ declares transaction boundary

Rollback
→ discard uncommitted transaction changes

readOnly = true
→ declares read-only intent

Propagation
→ defines how nested service transactions interact

Isolation
→ controls visibility between concurrent transactions
```

Transactions alone do not solve race conditions.

Concurrency tools include:

```
@Version
→ optimistic locking

PESSIMISTIC_WRITE
→ database locking

UNIQUE constraints
→ final database invariant protection
```

Reliability concepts connect naturally:

```
Retry
    ↓
needs idempotency

Database change + external message
    ↓
needs outbox

Outbox retries
    ↓
consumer should be idempotent
```

The complete mental model is:

```
Business Workflow
       ↓
Transaction Boundary
       ↓
Atomic database changes
       ↓
Concurrency protection
       ↓
Commit
       ↓
Outbox
       ↓
External side effects
       ↓
Idempotent processing
```

And the most important engineering rule from this module is:

**A good transaction boundary is the smallest boundary that completely protects the database consistency requirements of one business operation.**
