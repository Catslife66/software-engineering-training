# Caching

Caching means:

> Store frequently used data in a fast storage layer

So instead of:
```
API → Database → Response
```
we get:
```
API → Cache → Response
```

## Why cache exists

Without a cache:

```
User request
     ↓
API server
     ↓
Database query
     ↓
Return result
```

With a cache:

```
User request
     ↓
API server
     ↓
Cache check
   ├ hit → return result
   └ miss → query DB → store in cache
```

## Where Caching Happens

Caching can exist at multiple layers.

1. CDN Cache (closest to user)

    Examples:
    - Cloudflare
    - Fastly
    ```
    User → CDN → Response
    ```
2. Application Cache

    Example:
    - Redis
    ```
    API → Redis → Response
    ```

3. Database Cache

    Some databases cache query results internally.


**Example**:

```
User
 ↓
CDN
 ↓
Load Balancer
 ↓
API servers
 ↓
Redis cache
 ↓
Database
```

## Why Caching Helps Systems
1. Performance
```
Cache (memory) ≫ Database (disk)
```
2. Scalability
```
Cache absorbs traffic
DB handles fewer requests
```
3. Reliability
```
Cache protects DB from overload
```
Real Example
```
User → /trending-products
      ↓
Cache → return instantly
      ↓
(no DB hit)
```

## The Tradeoff of Caching

- Freshness
- Performance
- Complexity

## Cache Invalidation

Common strategies:
1. Time-based expiration

    Cache expires every 60 seconds

2. Write-through / update cache on write

    Write to DB → update cache

3. Cache invalidation
    DB update → delete cache entry