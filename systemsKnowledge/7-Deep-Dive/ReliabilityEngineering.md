# Reliability Engineering

Reliability engineering asks:

```
How systems fail
```

and more importantly:

```
How systems survive failure
```

**The Most Important Mindset Shift**

Junior thinking:

```
How do I make it work?
```

Senior thinking:

```
What happens when it breaks?
```

Because in production:

```
servers crash
databases slow down
networks fail
queues fill up
dependencies become unavailable
```

Failure is normal.

## 1. Timeouts = stop waiting

Let's start with a simple example.

Service A calls Service B:

```
Service A
    ↓
Service B
```

Question:

```
What if Service B never responds?
```

Without timeout:

```
Service A waits forever
```

**Solution**

Use a timeout.

Example:

```
Wait 3 seconds
```

If no response:

```
fail request
```

Mental model:

```
Never wait forever.
```

**Why timeouts matter**

Imagine:

```
1000 requests
```

all waiting forever.

Soon:

```
threads exhausted
memory grows
system slows
```

One slow dependency can kill the whole service.

## 2. Retries

Sometimes failures are temporary.

Example:

```
network hiccup
temporary overload
```

Retry may succeed.

Flow:

```
Request
↓
Fail
↓
Retry
↓
Success
```

**Why retries help**

Many failures are transient.

Example:

```
Database busy for 200ms
```

Retry later:

```
works
```

### Problem: Retry Storm

This is extremely important.

Imagine:

```
Service B is overloaded
```

Requests fail.

Everyone retries:

```
Retry
Retry
Retry
Retry
```

Now:

```
even more traffic
```

Service B gets worse.

Visual:

```
Failure
↓
Retries
↓
More load
↓
More failure
↓
More retries
```

This is **Retry Storm**

**Solution**

Exponential backoff.

Instead of:

```
retry immediately
```

Use:

```
1s
2s
4s
8s
```

between retries.

Mental model:

```
Give the system time to recover.
```

## 3. Circuit Breaker

One of the most famous reliability patterns.

Example

Service A depends on:

```
Payment Service
```

Payment Service goes down.

Without protection:

```
A keeps sending requests
A keeps timing out
```

Resources wasted.

Circuit breaker says:

```
Stop calling the failing service.
```

Think of electrical circuit breaker

When overloaded:

```
circuit opens
```

Current stops.

System version:

```
too many failures
↓
open circuit
↓
stop requests
```

### Circuit States

**Closed**

Normal.

```
Requests allowed
```

**Open**

Dependency unhealthy.

```
Requests blocked
```

**Half-open**

Test recovery.

```
Allow a few requests
```

If successful:

```
Close circuit
```

If failures continue:

```
Open again
```

Mental model:

```
Fail fast instead of waiting.
```

## 4. Bulkheads

Bulkheads are one technique that can help achieve failure isolation.

Named after ship compartments.

Damage one compartment:

```
rest of ship survives
```

Software version

Imagine:

```
Email Service
Billing Service
Checkout Service
```

All sharing:

```
same thread pool
```

Email floods system.

Now:

```
Checkout slows
Billing slows
Everything affected
```

**Solution**:

Separate resources.

Example:

```
Checkout threads
Billing threads
Email threads
```

Now:

```
Email failure
↓
Checkout still works
```

Mental model:

```
Isolate failures.
```

## 5. Graceful Degradation

Question:

```
Must everything fail?
```

Not necessarily.

Example

```
Recommendation Service down.
```

Bad:

```
Entire website unavailable
```

Better:

```
Hide recommendations
Continue shopping
```

Customer still:

```
browse
checkout
pay
```

Mental model:

```
Provide reduced functionality
instead of total failure.
```

## 6. Health Checks

Load balancer needs to know:

```
Which servers are healthy?
```

Example endpoint:

```
GET /health
```

Response:

```
{
  "status": "healthy"
}
```

If unhealthy:

```
remove from load balancer
```

No new traffic.

Mental model:

```
Don't send traffic to broken servers.
```

## 7. Failure Isolation

One of the most important principles.

Suppose:

```
Notification Service fails
```

Should checkout fail?

No.

Why?

Notifications are:

```
non-critical
```

Checkout is:

```
critical
```

Good design:

```
Checkout succeeds
Notification queued/retried later
```

Mental model:

```
Separate critical path from side effects.
```

You already saw this in:

```
Messaging
URL shortener
E-commerce
```

## 8. Real Example

E-commerce checkout:

```
Order Service
Inventory Service
Payment Service
Notification Service
```

Payment succeeds.

Notification fails.

❌ Bad:

```
Order fails
```

✅ Good:

```
Order succeeds
Notification retries later
```

## 9. Connection to Previous Modules

**Queue Module**

Retries:

```
good ✅
```

Retry storms:

```
bad ❌
```

Need:

```
backoff
```

**Cache Module**

Cache down.

Use:

```
graceful degradation
fallback to DB
```

**Saga Module**

Compensation:

```
reliability mechanism
```

for failures.

**Coordination Module**

Leader crashes.

Need:

```
leader election
```

to recover.

## Final Reliability Toolkit

When a dependency fails, ask:

**Timeout**

```
How long do we wait?
```

**Retry**

```
Should we try again?
```

**Backoff**

```
How fast should we retry?
```

**Circuit Breaker**

```
Should we stop calling it?
```

**Bulkhead**

```
Can failure spread?
```

**Graceful Degradation**

```
Can we continue with less functionality?
```

**Health Check**

```
Should traffic still go there?
```

## The Most Important Reliability Principle

This is worth memorizing:

```
Failures are inevitable.
Reliability is about limiting the blast radius.
```

Senior engineers rarely ask:

```
Can this fail?
```

They ask:

```
When this fails,
how much damage can it cause?
```
