# Engineering Trade-offs

**There is no perfect system.**

This is probably the biggest lesson in software architecture.

Every design improves something...

...while making something else worse.

For example:

```
Cache
```

Improves:

```
Response time
Database load
```

Makes worse:

```
Data freshness
Consistency
```

Or:

```
Strong consistency
```

Improves:

```
Correctness
```

Makes worse:

```
Latency
Availability
```

Or:

```
Message Queue
```

Improves:

```
Reliability
Decoupling
Throughput
```

Makes worse:

```
Complexity
Eventual consistency
Duplicate handling
```

## Drill 1

Imagine two engineers discussing this service.

Version A

```
public long getTotalUsers() {
    return repository.count();
}
```

Version B

```
private long cachedTotalUsers;

public long getTotalUsers() {
    return cachedTotalUsers;
}
```

Engineer A says:

Always use Version A.

Engineer B says:

Always use Version B.

Questions:

```
1. Do you agree with either engineer? Or neither? Why?
I'd agree neither because I don't have enough information to make a decision.

2. What advantage does Version A have?
Version A improves:
Correctness
Freshness
Simplicity

No cache means:
no invalidation
no TTL
no synchronization
no stale data

That's valuable.

3. What advantage does Version B have?
Version B improves:
Response latency
Throughput
Database protection
Scalability

4. Suppose this endpoint is called 10 requests per day, which version would you probably choose? Why?
I'd choose A because 10 requests per day is a very low frequency and it's not going to overload the database and we can ensure the data is correct at the other hand.

5. Now suppose it's called 200,000 requests per second, would your answer change? If so, why?
i'd choose B because at 200,000 requests per second, the database becomes a potential bottleneck. Serving reads from a cache significantly reduces database load, improves response latency, and allows the system to scale. Although caching introduces eventual consistency, that trade-off may be acceptable for this use case.
```

## Drill 2 - consistency vs latency

Imagine you're designing a banking system.

You have two choices.

Option A

```
Every transfer waits until:

both databases update,
every replica updates,
every cache updates,

before returning:

Transfer Successful
```

Option B

```
The transfer updates the primary database immediately.

Replicas and caches catch up a little later.

For a few seconds, different servers may return slightly different balances.

Eventually they all agree.
```

Questions:

```
1. Which option provides stronger consistency?
A because all components observe the same committed state before the operation is considered complete.

2. Which option provides better response latency?
B because the client does not wait for every component to finish updating before receiving a response.

3. Which option is likely to have higher availability?

Suppose one replica is temporarily unavailable.

Option A says:

I'm waiting...
I'm waiting...
I'm still waiting...

Eventually:
Timeout

The request may fail.

Option B says:

Primary database updated.
Return success.
Replica can catch up later.

That's why Option B generally improves availability.

4. Which option would you choose for:

- bank transfer go A because Money must never become incorrect.
- product view counter go B because Latency matters more.
- "likes" on a social media post go B because Eventual consistency is perfectly acceptable.

5. Why isn't one option simply "better" than the other?
Different systems have different business requirements. Some prioritise correctness and consistency, while others prioritise response latency, scalability, and availability. The appropriate design depends on the acceptable trade-offs for that workload.
```

## Drill 3

Imagine you're designing an online shop.

The product page shows:

```
MacBook Pro

★★★★★

Stock: 27

Price: £1,999

Description...
```

Your teammate says:

"Let's cache everything."

Option A

Every request reads directly from the database.

```
Browser
↓
Application
↓
Database
```

Option B

Everything is cached.

```
Browser
↓
Application
↓
Cache
```

The cache expires every 30 seconds.

Now imagine this happens.

Current stock:

```
Stock = 1
```

Customer A buys it.

One second later...

Customer B refreshes the page.

The cache still says:

```
Stock = 1
```

Questions:

```
1. Is the cache wrong? Or is it simply stale? What's the difference?
The cache contains stale data because the underlying inventory changed in the database, but the cached entry has not yet expired or been invalidated.

2. Should the product page always show perfectly up-to-date stock? Or is slight staleness acceptable? Why?
Browsing can tolerate eventual consistency; checkout requires stronger consistency.

3. Now imagine checkout. Customer B presses: Buy Now. Should checkout trust the cached stock? Why or why not?
The checkout path should use the authoritative inventory source and perform reservation atomically rather than trusting a potentially stale cache.

4. Suppose the checkout ignores the database and trusts the cache. What business invariant might be violated?
A successfully completed order must have valid inventory reservation, payment state, and order state.

5. Why do many e-commerce systems happily cache the product page, but never trust the cache during checkout?
Consistency requirements are operation-specific, not data-type-specific.
```

## Drill 4 - reliability vs duplicate

Imagine a payment provider sends this webhook:

```
payment_succeeded
paymentId = pay_123
orderId = order_456
```

Your service processes it and updates the order.

There are two possible designs.

Option A — Never retry

The provider sends the webhook once.

If your server is unavailable or the network fails:

```
Webhook lost
```

No second attempt.

Option B — Retry until acknowledged

The provider keeps retrying until your system sends an ACK.

That improves reliability, but now the same webhook may arrive more than once.

Example:

```
Webhook arrives
↓
Order marked paid
↓
ACK gets lost
↓
Provider retries
↓
Same webhook arrives again
```

Questions:

```
1. Which option has a higher risk of lost payment events?
Option A has the higher risk of message loss because there is no retry path. If delivery fails once, the payment event may never reach your system.

2. Which option has a higher risk of duplicate processing?
Option B has the higher risk of duplicate delivery because the sender retries until it receives an ACK.

3. For payments, which failure is usually more dangerous: losing the event or receiving it twice?
The payment itself may already have succeeded at the payment provider even if the webhook is lost. The danger is that the system never learns about the successful payment.

That can leave:
Payment provider: SUCCESS
Order system:     UNPAID

That is an inconsistent business state. So the dangerous part is that the successful payment event is lost.

4. If we choose retries, what property should the webhook handler have?
If we use at-least-once delivery, the webhook handler must be idempotent so that processing the same event multiple times produces the same business outcome.

For example:
event_id = evt_123

First delivery:
evt_123 not processed
→ update order
→ record evt_123

Retry:
evt_123 already processed
→ ignore duplicate

One more precision point: a duplicate payment_succeeded webhook usually should not charge the customer again if the charge already happened at the provider. More commonly, duplicate processing might mark the order paid twice, send duplicate emails, or trigger duplicate downstream work. Double charging is more likely if the original charge request itself is retried without idempotency.

That distinction matters:

Duplicate payment request
→ risk of double charge

Duplicate payment-success webhook
→ risk of duplicate side effects

5. Why is this another trade-off rather than a perfect solution?
At-least-once delivery improves reliability by reducing the chance of lost events, but it shifts complexity to the consumer because duplicate deliveries must be handled safely.

```

## Drill 5 - synchronous vs asynchronous processing

Imagine user registration does three things:

```
1. Save user to database
2. Send welcome email
3. Write analytics event
```

Option A — Do everything synchronously

```
Request
↓
Save user
↓
Send email
↓
Write analytics
↓
Return response
```

The user waits for all three steps.

Option B — Keep only the critical work synchronous

```
Request
↓
Save user
↓
Publish email job
↓
Publish analytics event
↓
Return response
```

Then workers handle email and analytics asynchronously.

Questions:

```
1. Which option gives lower response latency, and why?
Option B reduces end-to-end response latency because only the critical business operation remains on the request path. Side effects are delegated to asynchronous workers.

2. Which option gives better failure isolation?
Option B because non-critical work is separate from the main request path. So failures on sending email or generating analytics do not block the response and can retry asynchronously.

3. What new reliability problem does Option B introduce?
The system must guarantee eventual delivery of asynchronous work despite temporary failures such as worker crashes or network interruptions.

4. Which operations belong on the critical path here?
Persist User
↓
Registration Complete

5. Why isn’t asynchronous processing automatically “better” for every operation?

Benefits:
- Lower latency
- Better failure isolation
- Higher throughput
- Better scalability

Costs:
- More coordination
- Queues
- Retries
- Duplicate delivery
- Idempotency
- Monitoring
- Eventually consistent side effects
```

## Drill 6

Imagine your product manager says:

"Customers are complaining that checkout feels slow."

The checkout currently does this:

```
1. Reserve inventory
2. Charge payment
3. Create order
4. Send confirmation email
5. Update recommendation engine
6. Generate analytics
7. Notify warehouse
8. Update customer loyalty points
```

A junior engineer proposes:

"Let's make everything asynchronous!"

You're the backend engineer in the design review.

Questions:

```
1. Which steps should remain on the critical path?
Reserve inventory, Charge payment and Create order should remain on the critical path.

2. Which steps would you move off the critical path?
Send confirmation email
Update recommendation engine
Generate analytics
Notify warehouse
Update customer loyalty points

3. What business invariant are you protecting by keeping those critical steps synchronous?
If a order is placed successfully, the inventory must be reserved, the customer is charged and the order is created.

4. What new responsibilities do queues introduce?
Moving side effects to queues introduces delivery and operational responsibilities. We need an appropriate delivery guarantee, retry strategy, idempotent consumers, monitoring for failed or delayed jobs, and a way to handle messages that cannot be processed successfully.

5. Would you tell the product manager "Checkout is now faster." or "The customer receives a faster response."? Why are those two sentences different?
We reduced request-path latency rather than reducing the total amount of work performed by the system.

The system may still need the same total amount of work:
Email
Analytics
Warehouse notification
Loyalty points

We simply changed when the client has to wait for it.

6. Finish this sentence in your own words.
Good software architecture is about selecting trade-offs that preserve business invariants while meeting the system’s requirements for correctness, latency, availability, scalability, and reliability. There is rarely a universally best design; the appropriate design depends on the workload and the business constraints.
```
