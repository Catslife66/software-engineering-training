# Distributed Transactions

This is where things become really interesting.

Example:

```
Payment Service says:
"Payment succeeded"

Order Service says:
"Order creation failed"

Inventory Service says:
"Stock already reserved"
```

Now the system is inconsistent.

The big question becomes:

```
How do multiple services agree on one business operation?
```

## Simple World (Single Database)

Suppose everything is in one database.

```
BEGIN;

create_order();

reserve_inventory();

charge_payment();

COMMIT;
```

If anything fails:

```
ROLLBACK;
```

Result:

```
all succeed
or
all fail
```

This is ACID transaction behavior.

Easy.

## Distributed World

Now imagine:

```
Order Service      → Order DB
Inventory Service → Inventory DB
Payment Service   → Payment DB
```

Three services.

```
Three databases.
```

Question:

```
What if payment succeeds
but order creation fails?
```

Now we have:

```
Payment = success
Order = failed
Inventory = reserved
```

System inconsistent.

**Why This Is Hard**

Because:

```
Each service owns its own database.
```

A normal database transaction cannot span:

```
multiple independent databases
```

at least not easily.

**Example Failure**

Imagine:

```
1. Order created
2. Inventory reserved
3. Payment succeeds
4. Inventory service crashes before confirmation
```

Now:

```
Customer charged
Inventory uncertain
Order partially completed
```

Bad situation.

## First Attempt: 2-Phase Commit (2PC)

Historically, systems tried:

```
Two Phase Commit
```

### Phase 1 — Prepare

Coordinator asks everyone:

```
Can you commit?
```

Services respond:

```
YES
YES
YES
```

or

```
NO
```

### Phase 2 — Commit

If everyone says YES:

```
COMMIT
```

Otherwise:

```
ROLLBACK
```

Visual:

```
Coordinator
    ↓
Order Service     YES
Inventory         YES
Payment           YES

Coordinator
    ↓
COMMIT ALL
```

### Why 2PC Sounds Great

Guarantees:

```
all commit
or
all rollback
```

Exactly what we want.

### Why Modern Systems Avoid It

Because it creates:

```
slow coordination
blocking
availability issues
```

Example:

```
Payment ready
Inventory ready
Order ready

Coordinator crashes
```

Everyone waits.

Potentially forever.

### Mental Model

```
2PC prioritizes consistency
at the expense of availability
```

Very important connection to CAP thinking.

## Modern Solution: Saga Pattern

Most modern microservices use:

```
Saga Pattern

```

instead.

**Key Idea**

Instead of:

```
one big transaction
```

we do:

```
many local transactions
```

and compensate if something fails.

### Example Checkout Saga

Step 1:

```
Create Order
```

Step 2:

```
Reserve Inventory
```

Step 3:

```
Charge Payment
```

Step 4:

```
Mark Order Paid
```

**Failure Example**

Suppose:

```
Create Order ✅
Reserve Inventory ✅
Charge Payment ❌
```

Now what?

**Compensation Action**

Undo previous work.

```
Release Inventory
Cancel Order
```

Result:

```
system returns to valid state
```

### Mental Model

Normal transaction:

```
rollback
```

Saga:

```
compensating action
```

### Visual

Traditional:

```
A
↓
B
↓
C

failure

ROLLBACK
```

Saga:

```
A
↓
B
↓
C fails

undo B
undo A
```

### Example Compensation Table

| Action            | Compensation      |
| ----------------- | ----------------- |
| Create Order      | Cancel Order      |
| Reserve Inventory | Release Inventory |
| Charge Payment    | Refund Payment    |
| Create Shipment   | Cancel Shipment   |

## Why Sagas Scale Better

Services remain independent.

Each service:

```
owns data
owns transaction
```

No global lock.

No global coordinator.

Higher:

```
availability
scalability
```

**Tradeoff**

This is important.

For a brief period:

```
system may be inconsistent
```

Example:

```
Order exists
Inventory reserved
Payment failed
```

until compensation runs.

This is:

```
eventual consistency
```

again.

Notice how many deep dive topics connect together.

## Two Saga Styles

### Orchestration

Central coordinator:

```
Order Service
↓
Reserve Inventory
↓
Charge Payment
↓
Send Email
```

One service controls workflow.

### Choreography

Services communicate through events.

Example:

```
OrderCreated
↓
InventoryReserved
↓
PaymentSucceeded
↓
OrderCompleted
```

No central controller.

### Which Is Easier?

For most business systems:

```
Orchestration
```

is easier to understand.

For very large systems:

```
Choreography
```

can reduce coupling.

## Real World Examples

**E-commerce**

```
Order
Inventory
Payment
```

Uses Saga frequently.

**Airline Booking**

```
Seat
Payment
Reservation
```

Uses Saga.

**Hotel Booking**

```
Room
Payment
Booking
```

Uses Saga.

**The Biggest Insight**

In a monolith:

```
transaction = rollback
```

In distributed systems:

```
transaction = compensate
```

That sentence is worth remembering.

## Connection to Previous Modules

Notice how everything is now linked:

**Queue Module**

```
Events drive saga steps
```

**Consistency Module**

```
Saga creates eventual consistency
```

**Caching Module**

```
Compensations may invalidate cache
```

**Messaging Module**

```
Events move between services
```

## Final Mental Model

```
Single DB:
ACID transaction
rollback

Distributed system:
Saga
compensation
eventual consistency
```

## Checkpoint

Let's use your e-commerce system.

Suppose:

```
1. Order created ✅
2. Inventory reserved ✅
3. Payment failed ❌
```

```
1. Why can't a normal DB rollback solve this?
A database transaction only controls one database. In a distributed system, each service commits its own local transaction. A normal rollback cannot travel back in time and undo commits in other databases. That's why we need compensation actions, instead of rollback

2. Why is Saga considered eventually consistent rather than strongly consistent?
Saga is eventually consistent because each service commits its own local transaction independently.
If a later step fails, the system may temporarily be in an inconsistent state.
Compensation actions are then executed to undo previous work and bring the overall business process back to a valid state.
Therefore, consistency is achieved eventually rather than immediately.
```
