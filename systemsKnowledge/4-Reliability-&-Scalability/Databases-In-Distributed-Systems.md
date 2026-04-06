# Databases in distributed systems (Database scaling)

What happens when your database must scale?

Problem:

Even if you have: 10 API servers

If you have: 1 database

Then: all requests → 1 DB → bottleneck ❌

## Solution 1 — Read Replicas

```
Primary DB (writes)
       ↓
Replica DB (reads)
```

How It Works

```
Writes → Primary
Reads → Replicas
```

Example:

```
GET /products → Replica
POST /order → Primary
```

Benefit: scale read traffic, reduce load on primary DB

Problem: replication delay (lag)

This leads to: stale data

## Solution 2 — Sharding (Partitioning)

Split data across multiple databases.

```
User 1–1000 → DB A
User 1001–2000 → DB B
```

Benefit: scale write capacity, distribute load across multiple DBs

Problem:

```
complex queries
hard joins
data distribution logic
```

## Key Tradeoff

```
Scaling DB = complexity vs performance
```

Connection to Previous Topics

```
Cache → reduce DB load
Queues → reduce DB spikes
Replication → scale reads
Sharding → scale writes
```

## Key Insight

```
Replication = same data, multiple copies
Sharding = different data, split across nodes
```

## Mental Model

```
Single DB → simple but bottleneck
Distributed DB → scalable but complex

Sharding is more complex because data is split across multiple databases, requiring logic to determine where data is stored and how to retrieve it. This makes queries, joins, and transactions more difficult, and requires careful data distribution and routing strategies.
```
