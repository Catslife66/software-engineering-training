## What Happens During Replication

Normal flow:

```
Client request
     ↓
Write to Primary DB
     ↓
Primary sends update to Replica
```

So the replica is slightly behind the primary most of the time.

This delay is called **replication lag**.

## What the System Looks Like After Failover

After promotion:
Replica → New Primary
But the new primary is missing the latest transaction.

This is called:
**Replication inconsistency** or **data loss** during failover.

In distributed systems terminology, this relates to the **CAP tradeoffs** and consistency guarantees.

## How Systems Reduce This Risk

There are two main approaches.

1. Asynchronous Replication (Common)
   Primary writes immediately
   Replication happens afterward

Advantages:

- very fast writes
- high throughput

Disadvantages:

- possible data loss during failover

Most systems use this by default because performance is better.

2. Synchronous Replication
   Primary write
   ↓
   Replica confirms write
   ↓
   Transaction succeeds

So the data exists on multiple nodes before the client gets success.

Advantages:

- strong consistency
- no data loss

Disadvantages:

- slower writes
- higher latency

### The Tradeoff

This becomes a classic distributed systems tradeoff: Consistency vs Performance

Examples:

👉🏻Banking system
Needs strong consistency: Synchronous Replication

👉🏻Social media likes
Can tolerate small inconsistencies: Asynchronous replication

## Important Distributed Systems Idea

Designers always consider two metrics:

- RTO – Recovery Time Objective
- RPO – Recovery Point Objective

If asynchronous replication loses the last few seconds of data, the RPO might be: RPO = a few seconds
Some systems accept that.
Others cannot.
