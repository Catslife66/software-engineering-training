# Deep Login System

**Goal**

Design a login/authentication system and think about:

```
components
request flow
security
scaling
failure cases
```

## 1. Components

```
Client
Load balancer
API servers
Database
Session store or JWT signing/verification
Optional cache
Optional rate limiter
Optional audit log / queue
```

If session-based:

```
Redis session store
```

If JWT-based:

```
token signing key
```

## Flow

**Signup**

```
1. Client sends username/password over HTTPS
2. Server validates input
3. Server hashes password with salt using bcrypt
4. Server stores user record in database
```

**Login**

```
1. Client sends login request over HTTPS
   { username, password }

2. Load balancer routes request to an API server

3. API server looks up the user in the database

4. API server hashes the input password with the stored salt
   and compares it with the stored password hash

5. If valid, API server issues authentication
   - Session ID stored in session store, or
   - JWT returned to client

6. Server returns auth to client

7. Client stores it

8. Client includes auth in future requests

9. Any API server verifies auth on each protected request
```

## Security Design

### Password storage

bcrypt + salt

```
protect password at rest
slow brute-force attacks

```

### Transport security

HTTPS everywhere

```
protects credentials and tokens in transit.
```

### Cookie/token choice

**session cookie**

```
httpOnly
secure
sameSite configured carefully
CSRF protection
```

**JWT**

if in HttpOnly cookie:

```
better against XXS
still need CSRF protection
```

if in localStorage:

```
easier frontend control
higher XSS rish
```

### Extra protections

```
rate limiting on login
account lockout/backoff
2FA for sensitive systems
audit logs
short session lifetime
re-auth for sensitive actions
```

## Scaling Design

### Multiple API instances behind a load balancer

This requires stateless request handling.

**If using sessions:**

Redis

shared session store required, which adds infrastructure and can become a bottleneck.

**If using JWT:**

better fit for horizontal scaling, because each server can verify the token independently.

## Performance / Reliability Improvements

### Cache

Possible cache targets:

```
user profile metadata
permission lookup
revoked-token list if needed
```

Be careful not to cache sensitive auth state incorrectly.

### Queue

Use queue for non-critical async work:

```
login audit event
security notification
email alert
analytics
```

Do not put core password verification itself in a queue.

## Failure Cases

### Database slow/down

Effect:

```
login fails or becomes slow
```

Mitigation:

```
DB replicas for reads where appropriate
timeouts
connection pooling
monitoring
```

### Session store down

Effect:

```
session verification fails
users appear logged out
```

Mitigation:

```
Redis replication/failover
graceful error handling
```

### Token signing key leaked

Effect:

```
attacker may forge JWTs
```

Mitigation:

```
key rotation
short token TTL
secure secret management
```

### XSS vulnerability

Effect:

```
token theft if stored in localStorage
```

Mitigation:

```
HttpOnly cookies
output escaping
CSP
```

### CSRF

Effect:

```
browser sends cookie automatically on forged requests
```

Mitigation:

```
CSRF token
SameSite
Origin checks
```

## Tradeoff: Session vs JWT

**Session-based**

Best when:

```
strong server control
easy revocation
high-security systems
```

Tradeoff:

```
shared session store
harder scaling
```

**JWT-based**

Best when:

```
stateless APIs
horizontal scaling
distributed services
```

Tradeoff:

```
harder revocation
token lifetime risk
```

## Recommended Design by System Type

**Banking/internal admin**

```
Session-based auth
HttpOnly secure cookie
CSRF protection
short session TTL
2FA
```

**SPA + distributed API**

```
JWT or session cookie depending architecture
prefer HttpOnly cookie if possible
```

## Clean Architecture Summary

```
Client
↓
HTTPS
↓
Load balancer
↓
API servers
↓
User DB
↓
(optional Redis session store)

Async side effects:
API → Queue → audit/notification workers
```

## Mental model

```
Database → stores users and password hashes
HTTPS → protects transport
Auth layer → proves identity across requests
Load balancer + stateless design → enables scaling
```

## CHECKPOINT QUESTION

👉🏻 Why should login audit logging go to a queue, but password verification should not?

Login audit logging should go to a queue because it is a non-critical side effect of the login flow.

The system can complete authentication first and record the audit event asynchronously, which keeps the request path short and avoids delaying the user.

Password verification, however, is part of the critical path because the server must verify the user’s identity before granting access.

As a result, password verification must happen synchronously in the login request, while audit logging can be offloaded to a queue to improve responsiveness and reliability.
