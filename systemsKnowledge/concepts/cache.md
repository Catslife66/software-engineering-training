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

## A Useful Engineering Rule

Most systems can tolerate slightly stale data.
Few systems can tolerate a crashed database.

## Real Systems Use Multiple Layers of Caching

Architecture example:

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

## The Tradeoff of Caching

- Freshness
- Performance
- Complexity

## Cache Invalidation

Common strategies:
1️⃣ Time-based expiration
cache expires every 60 seconds
2️⃣ Write-through / update cache on write
DB update → update cache immediately
3️⃣ Cache invalidation
DB update → delete cache entry
