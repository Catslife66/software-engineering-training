# Database Consistency

This module explains **why distributed systems sometimes return old data**, and why companies intentionally accept that tradeoff.

## Start with a Single Database

Life is easy.

```
Application
    ↓
Database
```

Suppose:

```
UPDATE user
SET email = "new@email.com"
```

Immediately after:

```
SELECT email
```

returns:

```
new@email.com
```

Simple.

There is only:

```
one copy of data
```

## Now We Add a Replica

Why?

Because reads are expensive.

Earlier you learned:

```
Read traffic usually much larger than write traffic.
```

So we add:

```
          Primary DB
         /          \
        /            \
 Replica A      Replica B

```

Write:

```
UPDATE inventory
SET stock = 0
```

goes to:

```
Primary DB
```

Then replication occurs:

```
Primary
 ↓
Replica A
 ↓
Replica B
```

But replication takes time.

Even if:

```
10 ms
100 ms
500 ms
```

it is still not instant.

### Replica Lag

Replica lag means:

```
replica is behind primary
```

Example:

Primary:

```
stock = 0
```

Replica:

```
stock = 1
```

for a short period.

This is exactly what you saw with caching:

```
multiple copies of data
↓
consistency problem
```

## Eventual Consistency

Definition:

```
All replicas will eventually converge to the same value.
```

Not:

```
immediately
```

but:

```
eventually
```

Example

Time 0:

```
Primary = stock 0
Replica = stock 1
```

Time 1 second:

```
Primary = stock 0
Replica = stock 0
```

System becomes consistent again.

```
Temporary disagreement
↓
Eventually synchronized
```

## Strong Consistency

Strong consistency means:

```
Every read sees the latest successful write.
```

**Example:**

```
User changes password.
```

Immediately after:

```
GET password_status
```

must return:

```
updated
```

never:

```
old value
```

### Strong Consistency Tradeoff

To guarantee this:

```
system may need to wait
```

for replication.

That increases:

```
latency
```

and may reduce:

```
availability
```

**Mental model**

```
Strong consistency
↓
more coordination
↓
slower
```

## Eventual Consistency Tradeoff

Benefits:

```
fast
high availability
easy scaling
```

Cost:

```
temporary stale reads
```

**Example**

Social media likes.

You click:

```
Like
```

One user sees:

```
100 likes
```

Another sees:

```
101 likes
```

for a few seconds.

Nobody cares.

Because:

```
availability matters more
than perfect consistency
```

## Read-Your-Writes Consistency

This is a practical consistency model.

Requirement:

```
If I write data,
I should see my own write.
```

**Example**

User updates profile:

```
Name = Alice
```

Refresh page immediately.

Seeing:

```
Old name
```

feels broken.

Even if replicas are stale.

**Solution**

Route user's next reads to:

```
Primary DB
```

for a short period.

**Mental Model**

```
Not everyone sees latest data
But the writer does.
```

## Leader/Follower Replication

Most common database setup.

Architecture:

```
Leader (Primary)
↓
Followers (Replicas)
```

**Writes** always go to:

```
Leader
```

**Reads** can go to:

```
Leader
or
Follower
```

**Benefit**

```
read scaling
```

because:

```
many replicas
```

can answer queries.

**Cost**

```
replica lag
```

## Why Checkout Uses Primary

Remember e-commerce.

Browsing:

```
can use replicas
```

because:

```
small staleness acceptable
```

Checkout:

```
must use primary
```

because:

```
inventory must be current
```

This is why:

```
Product page → replica/cache
Checkout → primary
```

## Split-Brain (Concept Only)

Imagine:

```
Leader and follower lose connection
```

Both think:

```
"I'm the leader."
```

Now:

```
two leaders
```

accept writes.

Disaster.

This is called:

```
Split Brain
```

We'll revisit it later when we study distributed coordination.

For now remember:

```
Multiple leaders can create conflicting truths.
```

## Consistency in Systems You've Already Studied

**Messaging**

Realtime delivery:

```
eventual consistency acceptable
```

Offline messages:

```
must eventually appear
```

**Product Catalog**

```
eventual consistency acceptable
```

**Inventory**

```
strong consistency preferred
```

**Payments**

```
strong consistency required
```

**Bank Balance**

```
strong consistency required
```

## The Big Tradeoff

This is the most important thing to remember.

There is no:

```
perfect consistency
perfect availability
perfect scalability
```

simultaneously.

Engineers choose based on:

```
business requirements
```

**Real Examples**

Instagram Likes

- eventual consistency acceptable

Stock Trading

- strong consistency required

Inventory

- mostly strong consistency required

Search Results

- eventual consistency acceptable

## Final Mental Model

Think of consistency as:

```
How fresh must the data be?
```

Questions:

```
Can users see stale data?

If yes:
    eventual consistency is acceptable

If no:
    strong consistency is needed
```

## The Three Most Important Models

**Strong Consistency**

```
Latest write always visible
```

Tradeoff:

```
more coordination
more latency
```

**Eventual Consistency**

```
Data converges eventually
```

Tradeoff:

```
temporary stale reads
```

**Read-Your-Writes**

```
Writer sees their own updates
```

Tradeoff:

```
special routing logic
```

## CHECKPOINT

Senario

```
Primary inventory = 0

Replica inventory = 1
(200 ms replication lag)
```

User A just bought the last item.

```
1. What consistency problem exists?
The system is experiencing replica lag.
The primary database contains the latest inventory value (0), but the replica still contains an older value (1).
As a result, users reading from the replica may observe stale inventory data until replication catches up.

2. Why is product browsing usually okay?
Product browsing can tolerate some stale data because users are only viewing information, not making a final purchase decision.
Even if a user temporarily sees outdated inventory information, the system can still perform a final inventory check during checkout.
As a result, occasional stale reads during browsing are usually acceptable.

3. Why would checkout be dangerous if it used the replica?
Checkout requires the latest inventory, pricing, and discount information.
Because replicas may be behind the primary database, checkout could make decisions using stale data.
This may result in:
- overselling inventory
- incorrect pricing
- invalid discount application
Therefore, checkout should use the authoritative source of truth, typically the primary database.
```
