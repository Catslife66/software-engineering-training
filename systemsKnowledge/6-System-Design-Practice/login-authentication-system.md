# Login + Authentication

Design a simple login + authentication system

## Components

```
Client
API backend
Database
Optional session store (for session-based auth, e.g. Redis)
```

If using JWT:

```
Client
API backend
Database
(no shared session store required)
```

## Flow

```
1. Client sends login request over HTTPS
   { username, password }

2. API backend looks up the user in the database

3. Backend hashes the input password with the stored salt
   and compares it with the stored password hash

4. If valid, backend issues authentication
   - Session ID stored in session store, or
   - JWT returned to client

5. Client stores the auth credential
   - Session cookie, or
   - JWT in a secure storage strategy

6. Client includes auth in future requests

7. Backend verifies the session or JWT on each protected request

8. If valid, backend authorizes access to protected resources
```

## Security

- **HTTPS**

  Protects credentials and tokens in transit.

- **Hashed passwords**

  Protect stored credentials if the database is leaked.

- **Session / token validation**

  Ensures only authenticated users can access protected endpoints.

- More complete security view:
  ```
  HttpOnly cookies reduce XSS token theft
  CSRF protection is needed if using cookies
  Short token/session lifetime reduces damage if compromised
  ```

## Scaling considerations

- Multiple API instances behind a load balancer

  This requires stateless request handling.

- If using sessions:

  shared session store required, which adds infrastructure and can become a bottleneck.

- If using JWT:

  better fit for horizontal scaling, because each server can verify the token independently.

## Final mental model

```
Database → stores users and password hashes
HTTPS → protects transport
Auth layer → proves identity across requests
Load balancer + stateless design → enables scaling
```
