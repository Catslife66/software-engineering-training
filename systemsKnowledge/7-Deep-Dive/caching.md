# Caching

This is one of the most misunderstood topics in software engineering.

Most people learn:

```
Cache = faster
```

But real engineers learn:

```
Cache = consistency problems
```

❗️Important:

```
Cache is not a performance topic.
Cache is a consistency topic disguised as a performance topic.
```

**Engineer Vocabulary**

```
cache hit
cache miss
latency
database load
cache invalidation
stale data
TTL
cache stampede

frequently accessed data
fast storage layer
reduce read pressure
consistency challenges
underlying data
cache consistency
fall back to the database
populate the cache
cache entry
```

Caching is a technique that stores frequently accessed data in a fast storage layer to reduce database load and improve response latency.

When a request arrives, the application first checks the cache. On a cache hit, data is returned directly from the cache. On a cache miss, the application queries the database and may populate the cache with the result for future requests.

The main benefit of caching is improved performance and reduced read pressure on the database. However, caching introduces consistency challenges because cached data can become stale if the underlying data changes.

Common strategies such as TTL-based expiration, cache invalidation, and cache refresh mechanisms help maintain cache consistency. Systems must also consider failure scenarios such as cache stampedes, where many requests simultaneously fall back to the database after a cache entry expires.

## 1. Why caching becomes difficult

Let's start with something simple.

Imagine:

```
Database:
Product A = £100
```

Cache stores:

```
Product A = £100
```

User requests:

```
GET /products/1
```

Cache responds:

```
£100
```

Everything works.

Now an admin updates the product:

```
£100 → £120
```

Database now contains:

```
£120
```

But cache still contains:

```
£100
```

Question:

```
Which value will users see?
```

Answer:

```
£100
```

because cache is stale.

**First Big Insight**

Caching creates:

```
multiple copies of data
```

Before cache:

```
DB = single source of truth
```

After cache:

```
DB = truth
Cache = copy
```

The moment you have copies:

```
consistency becomes difficult
```

## 2. Cache Invalidation - how do we keep cache data correct

One of the famous jokes in software engineering:

```
There are only two hard things:

1. Cache invalidation
2. Naming things
```

Cache invalidation means:

```
How do we keep cached data correct?
```

**Example**

Database:

```
Product A = £120
```

Cache:

```
Product A = £100
```

Need:

```
remove old cache

or

update cache
```

❓Why is cache invalidation considered difficult?

```
Cache invalidation is difficult because the cache is a copy of the source of truth rather than the source of truth itself. When the underlying data changes in the database, the cache can become stale, creating cache consistency challenges.

The difficulty lies in ensuring that cached data is updated, invalidated, or expired at the correct time without introducing excessive complexity or database load. If invalidation is handled incorrectly, users may receive stale data; if it is too aggressive, many requests may fall back to the database and reduce the benefits of caching.
```

## 3. Cache Aside Pattern

This is the most common pattern.

Flow:

```
Request
↓
Check cache
↓
Hit → return
Miss → query DB
↓
Store result in cache
↓
Return result
```

Example:

```
GET /products/1
```

Cache miss:

```
DB queried
```

Cache updated:

```
product:1 = £120
```

Future requests:

```
served from cache
```

**Why companies like Cache Aside**

Because cache only stores data that users actually request.

## 4. Cache Invalidation in Cache Aside

When product updates:

```
Update DB
↓
Delete cache entry
```

Not:

```
Update cache first
```

Usually:

```
DB first
Cache second
```

Example:

```
UPDATE products
SET price = 120
```

Then:

```
DELETE cache["product:1"]
```

Next request:

```
cache miss
↓
read DB
↓
rebuild cache
```

## 5. TTL (Time To Live)

Many caches expire automatically.

Example:

```
product:1
TTL = 5 minutes
```

After 5 minutes:

```
cache removed
```

Next request:

```
rebuild from DB
```

**Benefits**

No manual cleanup required.

**Problem**

Data may remain stale for:

```
up to TTL duration
```

**Example**

TTL:

```
30 minutes
```

Price changes:

```
£100 → £120
```

Users may still see:

```
£100
```

for 30 minutes.

## 6. Cache Stampede

A very important failure mode.

Suppose:

```
Popular product page
```

Cache expires.

Immediately:

```
100,000 users request same page
```

What happens?

Without protection:

```
100,000 cache misses
↓
100,000 DB queries
```

DB gets crushed.

This is called:

```
Cache Stampede
```

Normal:

```
Cache
↑
many users
```

Stampede:

```
Cache expired
↓
everyone hits DB
```

## 7. Stampede Mitigations

### Option 1 — Rebuild lock

Only one request rebuilds cache.

Example:

```
Request A:
cache miss
acquires lock
queries DB

Request B:
waits

Request C:
waits
```

Once cache rebuilt:

```
everyone uses cache
```

### Option 2 — Early refresh

Refresh before expiration.

Example:

```
TTL = 60 minutes
refresh at 55 minutes
```

Users never see expiration.

### Option 3 — Stale-while-revalidate

Serve old cache briefly.

```
Return stale data
↓
Refresh in background
```

Good for:

```
product listings
news feeds
```

Not good for:

```
bank balances
inventory
```

## 8. Hot Keys

Another famous cache problem.

Imagine:

```
Taylor Swift concert tickets
World Cup final
Black Friday product
```

Everyone requests:

```
product:123
```

One cache key receives:

```
millions of requests
```

This is:

```
Hot Key
```

Problem:

```
Single cache node overloaded
```

Mitigations:

```
replication
sharding
local caches
```

## 9. Cache Consistency Tradeoff

You learned earlier:

```
consistency vs availability
```

Cache is one of the biggest examples.

You can choose:

**More consistency**

```
short TTL
frequent invalidation
```

Tradeoff:

```
more DB load
```

**More performance**

```
long TTL
aggressive caching
```

Tradeoff:

```
more stale data
```

## 10. Real Examples

**Good cache candidates**

```
Product catalog
Search results
Homepage data
Blog posts
User profile metadata
```

Because:

```
changes infrequently
read often
```

**Bad cache candidates**

```
Bank balance
Inventory count
Payment status
```

Because:

```
stale data dangerous
```

## Final Mental Model

```
Cache = copy of data
Copies become stale
Stale data creates consistency problems
```

## Most Important Things to Remember

```
1. Cache Aside is most common
2. Cache invalidation is hard
3. TTL trades freshness for simplicity
4. Cache stampede can overload DB
5. Hot keys can overload cache
6. Cache introduces consistency tradeoffs
```

## The Deep Insight

This is one of the most important system design principles:

```
Not all data has the same consistency requirements.
```

When designing systems, always ask:

```
What happens if this data is stale?
```

If answer is:

```
minor inconvenience
```

Cache aggressively.

If answer is:

```
money lost
inventory oversold
security issue
```

Use stronger consistency and avoid trusting cache.

**Example:**

| Data                | Can be Cached Aggressively? |
| ------------------- | --------------------------- |
| Product description | Yes                         |
| Product image       | Yes                         |
| Search results      | Usually                     |
| User profile        | Often                       |
| Inventory count     | Careful                     |
| Bank balance        | Usually No                  |
| Payment status      | Usually No                  |
