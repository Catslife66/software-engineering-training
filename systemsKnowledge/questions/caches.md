## Q: What happens when the cache server itself crashes.

Immediate consequence

Requests that were previously cheap now become expensive.

If the system was relying heavily on cache, the database suddenly receives a huge surge of traffic.

That can cause:

- sharp DB load increase
- slower queries
- saturated connection pool
- longer API response times
- timeouts
- retries
- cascading failure

## Why this is dangerous

A cache is often not just a performance optimization. It becomes a load shield for the database.

Once it disappears, the database may face traffic it was never meant to handle directly.

## Real systems defend against this

Engineers use several protections:

1. TTL jitter

Instead of all cache keys expiring at the same moment, they expire at slightly different times.

This prevents sudden synchronized misses.

2. Request coalescing

If many requests ask for the same missing key, only one request regenerates it, while others wait.

3. Stale-while-revalidate

Serve slightly stale cached data for a short time while refreshing in the background.

This is a very practical technique.

4. Rate limiting / load shedding

Reject some traffic early instead of letting the DB collapse.

5. Fallback behavior

Serve partial results, defaults, or degraded responses instead of hammering the DB.

---

## Q: Why might caching become less effective in this situation?

Suppose we cache property search results like:
"Manchester properties under £500k"
But thousands of users search with slightly different filters:
£480k
£490k
£500k
£510k
£520k

## Cache Fragmentation

Each variation produces a different cache key.
Example:

```
cache_key_1: manchester_price_500k
cache_key_2: manchester_price_490k
cache_key_3: manchester_price_480k
cache_key_4: manchester_price_510k
```

So instead of one cached result, the system now stores many similar ones.

This reduces the cache hit rate.

### Why This Happens

Search queries create high-cardinality keys.

High cardinality = many unique values.

Examples of high-cardinality inputs:

- price ranges
- geolocation coordinates
- timestamps
- search keywords
- user-specific data

Caching struggles when there are too many unique combinations.

## Memory Pressure

Systems like Redis store data in RAM, which is limited and expensive.
If thousands of unique queries are cached, memory usage grows quickly.

Caches must then start evicting entries.

**Common eviction policies include:**

- LRU (Least Recently Used)
- LFU (Least Frequently Used)

## Real Systems Use Smarter Caching Strategies

1. Cache Popular Queries Only
2. Cache Individual Objects
3. Normalize Query Parameters
