# Queue & Delivery Guarantees

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
1. What the queue will do

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
```
