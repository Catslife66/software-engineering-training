# Message Queues

A message queue is:

> A system that stores tasks and processes them asynchronously

Basic Flow
```
Producer → Queue → Worker
```
Example:
```
User signs up
   ↓
API pushes job → queue
   ↓
Worker sends email
```

## Why Queues Are Used
**1. Remove work from request path**

Without queue:
```
API → DB → send email → return
```
With queue:
```
API → DB → enqueue job → return
            ↓
        worker handles email
```

**2. Handle traffic spikes**

Example:
```
10,000 jobs → queue
workers process gradually
```

**3. Improve reliability**

If something fails:
```
job stays in queue → retry later
```

## Real Systems Use

Common tools:
- RabbitMQ
- Apache Kafka
- Redis (queues like BullMQ)

## Important Concepts
**1. Decoupling**

Producer does not depend on worker

**2. Retry mechanism**

failed job → retry

**3. Backpressure**

queue grows → system overloaded


## Tradeoffs
```
✔ scalable
✔ reliable
✔ decoupled
✖ delayed processing
✖ eventual consistency
✖ more complexity
```

## Real Example
```
Order placed
   ↓
Queue job:
   - send email
   - update analytics
   - notify warehouse
```

Queue = buffer for work

Workers = processors

## Key insight

A queue improves systems by:

- reducing request latency
- isolating failures
- smoothing traffic spikes
- moving non-critical work out of the request path

## Question practice

Question: What does it mean when a queue keeps growing continuously?

Answer:
```
// core idea
A growing queue indicates that the rate of incoming jobs exceeds the rate at which they are processed.

// how it works
This happens when the system’s processing capacity (workers or downstream services) cannot keep up with the workload.

// what happens in real systems
As a result, jobs accumulate, increasing latency, creating backpressure, and potentially leading to system overload or failure.
```

This is a **performance and capacity problem**, not just normal behavior.

If:
```
incoming jobs > processed jobs
```
Then:
```
queue size keeps increasing → system backlog → delays grow
```
Eventually:
```
system becomes overloaded
jobs delayed significantly
possible memory/resource issues
```

Common causes
```
too few workers
slow job processing
downstream service bottleneck
too many retries
```


### A Simple Framework

**1. Direct Answer**

Yes / No / Core idea

**2. Why (Mechanism)**

Explain how it works

**3. Impact (System effect)**

What happens in real systems

```
cause + mechanism + consequence
```