# URL Shortener

**Goal**

Design a system like:

```
bit.ly / tinyurl
```

User gives:

```
https://example.com/very/long/url
```

System returns:

```
https://sho.rt/abc123
```

## Requirements

**Functional requirements**

```
Create short URL from long URL
Redirect short URL to original URL
Optional: expiry time
Optional: custom alias
Optional: click analytics
```

**Non-functional requirements**

```
Fast redirect
High read traffic
Reliable
Scalable
```

## High-level components

```
Client
Load balancer
API servers
Database
Cache
Optional analytics queue/workers
```

Architecture:

```
Client
 ↓
Load balancer
 ↓
API servers
 ↓
Cache
 ↓
Database
```

Async analytics:

```
Redirect request
 ↓
Queue
 ↓
Analytics workers
```

## Core data model

A simple table:

```
short_code
long_url
created_at
expires_at (optional)
user_id (optional)
```

Example:

```
abc123 → https://example.com/very/long/url
```

## Write flow — create short URL

```
1. Client sends long URL to API
2. API validates URL
3. API generates short code
4. API stores {short_code, long_url} in DB
5. API returns short URL
```

Example:

```
POST /shorten
{ long_url: "https://example.com/very/long/url" }

→ returns
https://sho.rt/abc123
```

## Read flow — redirect

```
1. User visits https://sho.rt/abc123
2. Load balancer routes to API server
3. Server checks cache for abc123
4. If cache hit → get long URL
5. If cache miss → read DB → update cache
6. Return redirect response to browser
```

Response is usually:

```
HTTP 301 or 302 redirect
```

## Why cache is important

URL shortener is usually:

```
write-light
read-heavy
```

Many people click the same short link repeatedly.

So cache is ideal:

```
abc123 → long_url
```

Benefits:

```
fast redirects
reduced DB load
better scalability
```

## Code generation strategies

This is one of the main design questions.

**Option A — Auto-increment ID + Base62 encode**

Example:

```
DB ID: 125
Base62: cb
```

Why good:

```
simple
unique
short
```

Problem:

```
predictable IDs
```

**Option B — Random string**

Example:

```
aZ91Kd
```

Why good:

```
harder to guess
```

Problem:

```
must check collisions
```

**Option C — Hash of URL**

Why tempting:

```
same URL → same code
```

Problem:

```
collisions
less flexibility
```

## Good practical choice

A common simple design:

```
unique numeric ID → Base62 encode
```

Base62 uses:

```
a-z
A-Z
0-9
```

So short code stays compact.

## Scaling considerations

**Read scaling**

Use:

```
multiple API servers
load balancer
cache
read replicas
```

**Write scaling**

Usually lighter than reads, but if huge scale:

```
DB replication
possible sharding
```

## Analytics design

If you want click counts:

Do NOT do heavy analytics work in the redirect path if traffic is high.

Bad:

```
redirect request → DB write analytics synchronously
```

Better:

```
redirect request → send analytics event to queue → redirect immediately
```

Why:

```
keeps redirect fast
```

Workers later process:

```
click count
geo stats
device stats
```

## Failure cases

**Cache down**

Effect:

```
all reads hit DB
```

Mitigation:

```
DB scaling
rate limiting
graceful degradation
```

**DB down**

Effect:

```
cannot create new links
cache misses fail
```

Mitigation:

```
replication
failover
backups
```

**Code collision**

Effect:

```
two URLs same code
```

Mitigation:

```
unique constraint + retry
```

## Security / abuse concerns

Potential abuse:

```
spam
malware links
phishing
```

Mitigations:

```
rate limiting
auth for heavy usage
link scanning / abuse detection
expiration
blocklists
```

## Clean architecture summary

```
Create short URL:
Client → API → DB

Redirect:
Client → API → Cache → DB fallback → Redirect

Analytics:
Redirect → Queue → Worker
```
