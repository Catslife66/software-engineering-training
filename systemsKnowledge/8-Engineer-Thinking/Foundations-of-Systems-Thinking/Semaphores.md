# Semaphores

## 1. Mental Model

**Start with a mutex**

Imagine one shared printer.

Only one thread should use it at a time:

```
Printer capacity = 1
```

Conceptually:

```
Thread A ─┐
Thread B ─┼──→ [ Printer ]
Thread C ─┘
                capacity: 1
```

If A owns the lock:

```
A → using printer

B → WAIT
C → WAIT
```

This is the mental model we've already learned:

> A mutex provides exclusive access to a shared resource.

Now change the problem.

---

**A database can handle multiple requests**

Suppose an external database can safely handle:

```
maximum 10 concurrent expensive queries
```

We don't want:

```
capacity = 1
```

because that unnecessarily serializes everything.

But we also don't want:

```
capacity = unlimited
```

because 500 concurrent expensive queries could overwhelm the database.

What we actually want is:

```
capacity = 10
```

Conceptually:

```
Semaphore

Available permits:

[1][2][3][4][5][6][7][8][9][10]
```

A thread must acquire a **permit** before performing the protected operation.

```
Thread A
↓
acquire permit

10 → 9 permits available
```

Another thread:

```
Thread B
↓
acquire permit

9 → 8
```

Eventually:

```
10 threads currently performing queries

available permits = 0
```

Thread 11 arrives:

```
Thread 11
↓
acquire permit
↓
no permit available
↓
WAIT
```

When Thread 3 finishes:

```
Thread 3
↓
release permit

0 → 1
```

Now another waiting thread can proceed.

That's the core semaphore mental model:

> A semaphore controls how many concurrent operations are allowed to access a limited resource.

---

**Lock vs semaphore**

This distinction is worth getting completely clear.

A mutex asks:

> Who owns this resource exclusively?

A semaphore often asks:

> How many concurrent operations may proceed?

Conceptually:

```
Mutex

capacity = 1

[A]
```

versus:

```
Semaphore

capacity = N

[A][B][C][...]
```

A binary semaphore can have one permit, which makes it look similar to a mutex, but don't build your main mental model around that special case.

For our systems-thinking purposes:

```
Mutex
→ exclusive access

Semaphore
→ bounded concurrent access
```

## 2. Engineer Vocabulary

**Permit**

The conceptual authorization to enter the semaphore-controlled operation.

semaphore = 10 permits

means up to ten successful acquisitions can exist concurrently.

---

**Acquire**

A thread requests a permit.

```
semaphore.acquire()
```

If one is available:

```
permit acquired
→ proceed
```

If none are available:

```
no permit
→ wait
```

---

**Release**

Return a permit after the operation finishes.

```
semaphore.release()
```

This allows another waiting operation to proceed.

---

**Bounded concurrency**

This is probably the most important phrase in this topic.

> Bounded concurrency means allowing concurrent work while imposing an upper limit on how much can proceed simultaneously.

Instead of:

```
one at a time
```

or:

```
everyone at once
```

we choose:

```
up to N at once
```

---

**Concurrency limit**

The maximum number of operations allowed through simultaneously.

For example:

```
concurrency limit = 20
```

---

**Resource saturation**

A resource has reached the amount of concurrent work it can efficiently handle.

Examples:

```
database connections exhausted
API rate limit reached
CPU saturated
worker capacity exhausted
```

---

**Admission control**

Another useful engineering phrase.

Instead of allowing unlimited work into a constrained component, the system controls how much work is admitted.

Conceptually:

```
Incoming work
     ↓
Admission control
     ↓
Limited concurrency
     ↓
Downstream resource
```

A semaphore can be one mechanism for implementing admission control within an application.

---

**Concurrency throttling**

Deliberately restricting how many operations run concurrently to protect system capacity.

Don't confuse this with necessarily making the whole system slower.

Sometimes limiting concurrency actually improves overall system behaviour by preventing overload.

## 3. Engineer Explanation

Here's how an engineer might explain a semaphore:

```
A semaphore is a synchronization primitive used to bound concurrency. It maintains a number of permits, and an operation must acquire a permit before proceeding. Once all permits are in use, additional callers wait until another operation releases one. This allows the system to protect a capacity-constrained resource without serializing access completely.
```

And the systems explanation:

```
Semaphores are useful when a downstream resource supports concurrent access but has finite capacity. Rather than allowing unbounded concurrency to overwhelm the dependency, we impose a concurrency limit and apply admission control before work reaches it.
```

That second explanation is the one I particularly want you to absorb.

Semaphores aren't interesting because:

```
they contain a counter.
```

They're interesting because:

**they let architecture reflect capacity constraints.**

## 4. Trade-offs

Suppose our database comfortably handles 20 expensive queries concurrently.

### No limit

```
500 requests
↓
500 concurrent queries
↓
database
```

Possible consequences:

```
database saturation
connection exhaustion
higher query latency
timeouts
cascading failures
```

So perhaps we introduce:

```
Semaphore permits = 20
```

Now:

```
500 requests
↓
20 admitted
480 waiting
↓
database
```

**Benefits**

We gain:

- bounded concurrency
- protection for downstream capacity
- more predictable resource utilisation
- reduced overload risk
- better failure containment

But there's a cost.

Those 480 requests haven't disappeared.

They're waiting.

So:

```
protect downstream
        ↓
limit concurrency
        ↓
excess work waits
        ↓
queueing / waiting latency
```

This gives us the trade-off:

> A semaphore trades unrestricted concurrency for controlled resource utilisation and overload protection.

### Why not just use one permit?

Because then:

```
500 requests
↓
1 at a time
```

The database might safely support 20.

We've unnecessarily reduced throughput.

### Why not 500 permits?

Because then we've effectively removed the protection.

So semaphore sizing is another engineering capacity decision:

```
too small
→ underutilisation
→ unnecessary waiting
→ lower throughput

too large
→ downstream saturation
→ latency
→ timeouts
→ failures
```

There is no magical universally correct number.

Sound familiar?

> The correct limit depends on the capacity and behaviour of the system being protected.

## 5. Failure Modes

### Permit leak

This is one of the classic semaphore problems.

Imagine:

```
acquire permit
↓
call database
↓
exception!
↓
method exits
```

But we forgot:

```
release permit
```

The semaphore may now think a permit is still in use.

Repeat this enough:

```
10 permits

exception → 9
exception → 8
exception → 7
...
exception → 0
```

Eventually:

```
no operations can proceed
```

even though the underlying database might be perfectly healthy.

That's a **permit leak**.

This is why cleanup usually needs to happen reliably.

Conceptually in Java:

```
semaphore.acquire();

try {
    performWork();
} finally {
    semaphore.release();
}
```

The `finally` matters because the permit should be returned even when the operation fails.

### Too-low concurrency limit

```
database capacity = 50

semaphore permits = 2
```

We're protecting the database far more than necessary.

Result:

```
large waiting backlog
↓
higher latency
↓
poor resource utilisation
↓
lower throughput
```

### Too-high concurrency limit

```
database capacity ≈ 20

semaphore permits = 500
```

The semaphore provides little useful protection.

Result:

```
too much concurrent work
↓
downstream saturation
↓
latency increases
↓
timeouts
```

### Waiting without bounds

This one is architecturally important.

Suppose:

```
incoming rate = 1,000 requests/sec

processing capacity = 200 requests/sec
```

A semaphore can limit concurrent processing.

But it cannot magically make:

```
1000/sec
```

fit into:

```
200/sec
```

Excess work accumulates.

```
arrival rate > completion rate
        ↓
waiting backlog grows
        ↓
end-to-end latency grows
        ↓
eventual resource exhaustion
```

Recognise that?

It's the same reasoning we learned with queues and backpressure.

So:

> Concurrency limiting protects capacity, but it does not solve sustained overload.

That's an important engineer sentence.

Eventually the system may need:

- bounded queues
- request rejection
- timeouts
- rate limiting
- load shedding
- more capacity

We'll study these mechanisms more deeply in Scaling Thinking.

## 6. Real Systems

Let's use a backend example.

Imagine a Spring Boot service calling a third-party fraud API.

Your application can handle:

```
1,000 concurrent requests
```

but the fraud provider comfortably supports only:

```
50 concurrent calls
```

Without protection:

```
1000 application requests
          ↓
1000 fraud API calls
          ↓
third-party service overloaded
          ↓
timeouts
          ↓
your threads remain blocked
          ↓
your own service begins degrading
```

Notice the propagation:

```
Downstream overload
↓
slow responses
↓
your worker threads wait longer
↓
thread pool saturation
↓
your response latency increases
↓
your service becomes unhealthy
```

This is how failures can cascade through distributed systems.

Now introduce:

```
Semaphore permits = 50
```

Conceptually:

```
1000 requests
       ↓
[ Semaphore: max 50 ]
       ↓
Fraud API
```

At most 50 operations are allowed through concurrently.

We've created a **bulkhead-like** boundary around the dependency.

The name comes from ships: compartments prevent one flooded section from sinking everything.

You don't need to learn resilience-pattern libraries yet. Just recognize the architectural principle:

> Constrain resource usage so failure or saturation in one dependency does not consume unlimited resources in the rest of the system.

---

**Database connections**

You've actually seen semaphore-like capacity control without calling it a semaphore.

A database connection pool might contain:

```
20 connections
```

Requests acquire a connection:

```
Request
↓
acquire DB connection
↓
query
↓
release connection
```

If all 20 are busy:

```
Request 21
↓
wait
```

Conceptually, this is very similar to our semaphore model:

```
finite capacity
↓
acquire
↓
use
↓
release
```

The actual implementation details differ, but the systems reasoning is the same.

## 7. Communication Training

You're reviewing a service.

The application has:

```
200 worker threads
```

Every request calls an external recommendation service.

During peak traffic:

```
Application CPU:       40%
Worker threads:        heavily occupied
Recommendation API:    response time rises from 100ms → 5 seconds
Application latency:   rises dramatically
```

The teammate proposes:

> "Let's increase our thread pool from 200 to 500 so we can process more requests concurrently."

You discover the recommendation service starts becoming unstable above approximately:

```
50 concurrent requests
```

You're the backend engineer.

Explain:

1. Where does the bottleneck appear to be?
2. Why might increasing the worker-thread pool make the situation worse?
3. How could a semaphore help?
4. What would happen to request 51 when all 50 permits are in use?
5. Does introducing the semaphore actually increase the recommendation service's capacity?
6. What new problem appears if incoming traffic continuously exceeds those 50 concurrent slots?

Try naturally using:

```
bounded concurrency
concurrency limit
permits
acquire
release
downstream dependency
resource saturation
admission control
backpressure
response latency
```

As before: reason first. I'll refine your engineer language afterward.

## 8. Technology Spotlight — Java Semaphore

Java provides:

```
java.util.concurrent.Semaphore
```

Conceptually:

```
Semaphore semaphore = new Semaphore(50);
```

means:

```
50 permits
→ up to 50 successful acquisitions concurrently
```

A simplified pattern looks like:

```
semaphore.acquire();

try {
    recommendationService.call();
} finally {
    semaphore.release();
}
```

Mental model:

```
Request
↓
acquire permit
↓
call dependency
↓
release permit
```

If no permit is available:

```
Request
↓
wait
```

At this stage, don't worry about memorising the Java API. Your Java classroom can teach implementation details later.

Here we care about:

> Why would an engineer introduce a semaphore in the first place?

---

# Handbook Page — Semaphores

## Mental Model

A semaphore bounds concurrency by controlling how many operations may access a constrained resource simultaneously.

```
Incoming operations
        ↓
Semaphore
[N permits]
        ↓
Constrained resource
```

Operations:

```
acquire
→ consume permit

release
→ return permit
```

When:

```
available permits = 0
```

additional operations must wait or otherwise be handled according to the design.

## Mutex vs Semaphore

```
Mutex
→ exclusive access
→ generally one owner

Semaphore
→ bounded concurrent access
→ N permits
```

Think:

> Mutex protects exclusivity. Semaphore controls capacity.

## Key Vocabulary

```
semaphore
permit
acquire
release
bounded concurrency
concurrency limit
admission control
concurrency throttling
resource saturation
downstream dependency
permit leak
```

## Engineering Principles

1. Concurrency should respect downstream capacity.

2. More concurrent work does not create more downstream capacity.

3. A semaphore allows concurrency without allowing it to become unbounded.

4. The concurrency limit should reflect the capacity and behaviour of the protected resource.

5. Permits must be reliably released, including during failures.

6. Limiting concurrency protects a resource but does not solve sustained overload.

## Failure Thinking

When a dependency becomes slow, ask:

```
How much concurrent work are we sending?

What is the dependency's capacity?

Are callers accumulating?

Are worker threads becoming blocked?

Is the concurrency limit too high?

Is it too low?

Are permits being leaked?

Is incoming demand greater than sustainable throughput?
```

## Engineer Explanation

```
A semaphore is a synchronization mechanism for bounded concurrency. It limits the number of operations that can access a capacity-constrained resource simultaneously. This can protect downstream dependencies from saturation and prevent unbounded resource consumption, but excess work still has to wait, so sustained overload can create growing latency and backpressure.
```
