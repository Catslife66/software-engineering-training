# Distributed Coordination

The Core Problem

Imagine we have:

```
Worker A
Worker B
Worker C
```

All consuming jobs from a queue.

Suppose a job says:

```
Generate monthly invoice for Customer 123
```

Question:

```
How do we make sure only ONE worker generates it?
```

Because if:

```
Worker A generates invoice
Worker B generates invoice
```

Then:

```
customer gets charged twice ❌
```

## 1. Why coordination exists

Single server:

```
One process
One decision maker
```

Easy.

Distributed system:

```
Many servers
Many workers
Many databases
```

Now we need:

```
agreement
```

**Mental Model**

Distributed coordination is about:

```
Who gets to do the work?
```

## 2. Distributed Lock

The simplest coordination tool.

Think:

```
door lock
```

Only one person can enter.

Example:

Invoice Job

```
Worker A:
acquires lock

Worker B:
fails to acquire lock

Worker C:
fails to acquire lock
```

Result:

```
only Worker A executes
```

Visual:

```
Lock: invoice_123

Worker A → owns lock ✅
Worker B → denied ❌
Worker C → denied ❌
```

**Why not local memory?**

Bad:

```
Worker A memory:
"I own lock"
```

Worker B:

```
doesn't know
```

Worker C:

```
doesn't know
```

Need:

```
shared coordination store
```

Usually:

```
Redis
ZooKeeper
etcd
```

## 3. Lock Expiration

Huge problem.

Imagine:

```
Worker A acquires lock
```

Then:

```
Worker A crashes
```

Lock never released.

Now:

```
everyone blocked forever
```

**Solution:**

```
TTL
```

Example:

```
lock expires after 30 seconds
```

If owner dies:

```
lock automatically released
```

**Mental model:**

```
Every distributed lock needs an expiry.
```

## 4. Inventory Example

Remember inventory reservation?

Suppose:

```
Stock = 1
```

Two checkout requests arrive.

Without coordination:

```
Checkout A reserves
Checkout B reserves
```

Oversold.

**With lock:**

```
Lock inventory:123

Checkout A acquires lock
↓
reserve inventory
↓
release lock

Checkout B waits
↓
checks inventory
↓
stock gone
```

Safe.

## 5. Leader Election

Another huge concept.

Imagine:

```
Server A
Server B
Server C
```

Need:

```
only one scheduler
```

For example:

```
daily billing
daily cleanup
nightly report
```

Question:

```
Which server runs the job?
```

If all run:

```
3 reports
3 billings
3 cleanups
```

Bad.

**Solution:**

```
elect one leader
```

Example:

```
Leader = Server B
```

Only leader executes scheduled jobs.

**Mental model:**

```
Leader = decision maker
Followers = standby
```

## 6. Leader Failure

What happens if:

```
Leader crashes?
```

Need:

```
new leader
```

This process is:

```
Leader Election
```

Example:

```
Leader = B
```

B dies.

System elects:

```
Leader = C
```

Work continues.

**Why this matters**

Many systems need:

```
one coordinator
```

Examples:

```
job scheduling
cluster management
database leaders
```

## 7. Split Brain

This is a famous distributed systems problem.

Imagine:

```
Server A
Server B
```

Network failure happens.

A cannot talk to B.

A thinks:

```
"I'm leader"
```

B thinks:

```
"I'm leader"
```

Now:

```
two leaders
```

Both process work.

Disaster.

Example:

```
Inventory updated twice
Payments processed twice
```

This is called **Split Brain**

**Mental model:**

```
Two truths exist simultaneously.
```

## 8. Why Split Brain Is Dangerous

Distributed systems rely on:

```
single source of authority
```

Split brain creates:

```
multiple authorities
```

Now:

```
conflicting writes
duplicate processing
inconsistent state
```

## 9. Consensus (High Level)

Consensus means:

```
multiple machines agree on one decision
```

Example:

```
Who is leader?
```

Need all healthy nodes to agree.

Consensus systems:

```
ZooKeeper
etcd
Raft
Paxos
```

You don't need the algorithms yet.

Only the idea.

**Mental model:**

```
Consensus = reaching agreement despite failures.
```

## 10. Real Systems You Already Know

**Queue Consumers**

Need:

```
avoid duplicate processing
```

May use:

```
locks
consumer groups
```

**Inventory Reservation**

Need:

```
one reservation wins
```

May use:

```
transaction
atomic update
lock
```

**Scheduled Jobs**

Need:

```
one server runs job
```

May use:

```
leader election
```

**Database Primary**

Need:

```
one primary database
```

May use:

```
leader election
```

## Tradeoffs of Distributed Locks

Benefits:

```
prevent duplicate work
protect shared resources
```

Costs:

```
extra coordination
latency
lock contention
risk of deadlock/expiration issues
```

## Most Important Principle

This is worth memorizing:

```
Coordination improves correctness
but reduces scalability.
```

Why?

Because every coordination step requires:

```
communication
waiting
agreement
```

which slows the system.

## Final Mental Models

**Distributed Lock**

```
Who can do the work?
```

**Leader Election**

```
Who makes decisions?
```

**Split Brain**

```
What happens if multiple leaders exist?
```

**Consensus**

```
How do machines agree?
```

## The Big Insight

Notice how this topic connects to previous modules.

**Queue Module**

Problem:

```
same message processed twice
```

Solution:

```
idempotency
```

**Saga Module**

Problem:

```
multiple services coordinating work
```

Solution:

```
compensation
```

**Coordination Module**

Problem:

```
multiple servers doing the same work
```

Solution:

```
leader election
locks
consensus
```

Different problem.

Different solution.
