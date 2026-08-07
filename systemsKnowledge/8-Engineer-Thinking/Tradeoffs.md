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

## Drill 2

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

2. Which option provides better response latency?

3. Which option is likely to have higher availability?

4. Which option would you choose for:

- bank transfer
- product view counter
- "likes" on a social media post

Explain why.

5. Why isn't one option simply "better" than the other?
```
