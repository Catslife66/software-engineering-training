# Queue & Delivery Guarantees

> A message queue acts as a durable buffer between producers and consumers.

**Engineer Vocabulary**

```
producer
consumer

publish messages
consume messages

asynchronous processing
decouple

critical path
main request path

durable buffer
durable message storage
persisted message

traffic spikes
bursty workload
backpressure: incoming rate > processing rate
queue backlog

incoming rate
processing capacity

delivery guarantees
At-most-once
At-least-once
idempotency

failure isolation
downstream service

temporarily unavailable
temporary failure

end-to-end latency
under-provisioned consumers -> not enough workers/resources
service degradation
```

```
End-to-end latency is the total time required for a request or operation to travel through the entire system and produce a result.

A queue backlog is the accumulation of messages waiting to be processed by consumers.

A bursty workload is a traffic pattern characterized by short periods of extremely high activity followed by periods of lower activity.

100 requests/sec
100 requests/sec
10,000 requests/sec
100 requests/sec

Backpressure is the pressure caused by the mismatch between incoming work and processing capacity.

Incoming > Processing
↓
Backpressure occurs
↓
Queue Backlog grows
↓
End-to-End Latency increase
↓
Possible Service Degradation
```

Message queues enable asynchronous processing by decoupling producers from consumers. Instead of performing non-critical work synchronously, producers publish messages to a queue and consumers process them independently.

This helps smooth traffic spikes, manage backpressure, and improve reliability because temporary failures in downstream services do not immediately impact the main request path.

Message queues often provide at-least-once delivery guarantees, ensuring that messages are eventually processed even if failures occur. However, this introduces the possibility of duplicate message delivery, so consumers typically need to be idempotent.

## 1. Why queues exist

Queues help:

```
smooth traffic spikes
decouple systems
process work asynchronously
```

Example:

```
signup → queue → send welcome email
```

instead of:

```
signup waits for email sending
```

## 2. The real distributed systems problem

The difficult part is NOT:

```
sending messages
```

The difficult part is:

```
knowing whether a message was processed correctly
```

Because failures happen everywhere:

```
network failure
server crash
consumer crash
ACK lost
retry
duplicate delivery
```

## 3. Delivery guarantees

There are 3 major delivery models.

### A. At-most-once

Meaning:

```
message delivered 0 or 1 time
```

No retries.

If failure happens:

```
message may be LOST
```

Example:

```
send analytics event
```

If one analytics event disappears:

```
acceptable
```

**Mental model**

```
Never duplicate
But may lose
```

### B. At-least-once

Meaning:

```
message delivered 1 or more times
```

System retries if uncertain.

Result:

```
duplicates possible
```

Example:

```
payment webhook
```

Payment providers usually retry webhooks.

Why?

Because losing payment confirmation is dangerous.

Better:

```
duplicate event
```

than:

```
lost payment
```

**Mental model**

```
Never lose
But may duplicate
```

### C. Exactly-once (The dangerous phrase)

Meaning:

```
message processed exactly one time
```

This sounds ideal.

But in distributed systems:

```
true exactly-once is extremely difficult
```

and often impossible globally.

Most systems actually implement:

```
at-least-once delivery
+
idempotent processing
```

which creates:

```
exactly-once EFFECT
```

## 4. Why duplicates happen

Example:

```
Consumer receives message
↓
Processes successfully
↓
Consumer crashes BEFORE ACK
```

Queue thinks:

```
"not processed"
```

So it retries.

Now same message processed twice.

## 5. ACK (Acknowledgement)

This is critical.

Flow:

```
Queue sends message
↓
Consumer processes
↓
Consumer sends ACK
```

ACK means:

```
"I processed this successfully"
```

Only then should queue remove the message.

## 6. The impossible timing problem

What if:

```
consumer processed successfully
BUT ACK was lost
```

Queue retries.

Duplicate created.

This is why:

```
distributed systems naturally create duplicates
```

## 7. Idempotency (MOST IMPORTANT CONCEPT)

Idempotency means:

```
processing same message multiple times gives same final result
```

**Example — BAD**

Webhook:

```
payment_succeeded
```

Processing logic:

```
add $100 balance
```

If webhook duplicates:

```
balance becomes $200 ❌
```

**Example — GOOD**

Instead:

```
if payment_id already processed:
    do nothing
```

Now duplicate safe.

## 8. Idempotency key

Usually:

```
event_id
message_id
payment_id
client_request_id
```

stored in DB with uniqueness constraint.

Example

```
processed_events:
payment_123
payment_456
```

If same event arrives again:

```
ignore safely
```

## 9. Queue retry strategy

Retries are important.

But dangerous.

**Bad retry strategy**

```
retry immediately forever
```

This creates:

```
retry storms
system overload
```

**Better strategy**

```
exponential backoff
```

Example:

```
retry after:
1 sec
2 sec
4 sec
8 sec
```

This protects the system.

## 10. Poison messages

Some jobs always fail.

Example:

```
invalid data
corrupted payload
buggy job
```

Retrying forever is useless.

**Solution: Dead Letter Queue (DLQ)**

After too many failures:

```
move message to DLQ
```

Purpose:

```
manual inspection
avoid infinite retry loop
```

## 11. Message ordering problem

Queues can reorder messages.

Especially:

```
multiple consumers
parallel processing
retries
```

Example

```
Message A
Message B
```

But consumer processes:

```
B before A
```

**Solutions**

Depends on requirements.

Possible approaches:

```
partition by user/chat/order
single consumer per partition
sequence numbers
```

Tradeoff:

```
better ordering
↓
less parallelism
```

## 12. Backpressure

Problem:

```
incoming jobs > processing speed
```

Queue grows forever.

**Symptoms**

```
latency increases
memory pressure
workers overloaded
timeouts
```

**Solutions**

```
add consumers
rate limit producers
shed load
split queues
prioritize jobs
```

## 13. Queue mental models

**Queue = traffic buffer**

```
absorbs spikes
```

**Queue ≠ magic scaling solution**

If workers cannot keep up:

```
queue becomes bottleneck
```

**Retry creates duplicates naturally**

So:

```
idempotency is mandatory
```

## 14. Real-world examples

**At-most-once**

```
analytics tracking
metrics events
```

Loss acceptable.

**At-least-once**

```
payments
orders
email sending
inventory events
```

Duplicates safer than loss.

**Idempotent processing**

```
payment webhooks
order creation
inventory deduction
```

## 15. The biggest deep insight

This is the key transition into distributed systems thinking:

```
Distributed systems prefer:
duplicate work > lost work
```

Because:

```
duplicates can be handled
lost critical data often cannot
```

## 16. Core principles to remember

```
1. ACK after successful processing
2. Retries create duplicates
3. Idempotency is essential
4. Backpressure kills systems
5. DLQ prevents infinite retries
6. Ordering reduces scalability
```

## CHECKPOINT

```
1. What the queue will do when ACK is lost
Because the queue did not receive the ACK, it assumes the webhook processing may have failed.
As a result, the queue retries delivery of the same webhook event to guarantee the event is eventually processed.


2. Why duplicate processing may happen
The webhook may actually have been processed successfully already, but the ACK was lost due to network failure or consumer crash.
Since the queue cannot distinguish between:
- "processing failed"
and
- "ACK failed"
it retries the event, which can cause the same payment event to be processed multiple times.


3. How idempotency prevents damage
Each webhook event contains a unique event ID.
Before processing, the consumer checks whether this event ID has already been processed and stored.
If the event already exists, the consumer safely ignores the duplicate retry.

As a result, retries do not create duplicated side effects such as:
- charging twice
- deducting inventory twice
- sending duplicate confirmations


4. Why are message queues commonly used in microservices architectures?
Message queues are commonly used in microservices architectures because they decouple producers from consumers and enable asynchronous processing. By removing non-critical work from the critical path, services can respond more quickly and remain isolated from downstream failures.

Message queues act as a durable buffer, allowing messages to remain persisted even when consumers are temporarily unavailable. They also help manage traffic spikes and backpressure by buffering work when the incoming rate exceeds processing capacity.

Most queue systems provide at-least-once delivery guarantees, which improves reliability but introduces the possibility of duplicate message delivery. As a result, consumers are typically designed to be idempotent.


5. Why is a growing queue often considered a warning sign?
A growing queue is typically a warning sign that the incoming rate of work exceeds the processing capacity of consumers.

As messages accumulate, a queue backlog forms, increasing the time required for work to be processed. This increases end-to-end latency and is often a sign of backpressure in the system.

If the backlog continues to grow, it may indicate that consumers are under-provisioned, unhealthy, or unable to keep up with demand, which can eventually lead to service degradation or failures.

```
