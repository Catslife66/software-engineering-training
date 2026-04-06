# Authentication (Sessions vs Tokens)

How systems keep users logged in after login?

Because right now: login = one request

But real systems need: user stays logged in across many requests

**Problem:**

HTTP is stateless:

```
Request 1 → login
Request 2 → who are you?
```

Server forgets.

Solution: 👇

## Authentication Mechanisms

### 1. Session-Based Authentication

Flow

```
Login:
User → server
Server → creates session
Server → stores session (DB / Redis)
Server → sends session ID (cookie)
```

Next requests

```
Browser sends cookie
Server → looks up session → identifies user
```

Characteristics

```
✔ simple
✔ server-controlled
✖ requires server storage
✖ harder to scale
```

### 2. Token-Based Authentication (JWT)

Flow

```
Login:
Server → generates token (JWT)
Server → sends token to client
```

Next requests

```
Client sends token
Server → verifies token → identifies user
```

Characteristics

```
✔ stateless
✔ scalable
✖ harder to revoke
✖ more complex
```

## Key Difference

```
Session → server stores state
JWT → client stores state
```

## Connection to What You Learned

```
Stateless HTTP → requires authentication layer
Load balancing → prefers stateless (JWT)
Distributed systems → avoid shared session storage
```

## Mental Model

```
Session = server remembers you
JWT = you carry your identity

Sessions → stateful → harder to scale
JWT → stateless → easier to scale
```

## CHECKPOINT

### **1️⃣ Why do sessions make horizontal scaling harder?**

Sessions make horizontal scaling harder because session data is stored on the server side (e.g., in memory or a database).

When multiple server instances are used, each request may hit a different server, so all servers must share access to the same session store.

As a result, this introduces additional dependency on shared storage (like a database or Redis), which can become a bottleneck and reduce scalability.

### **2️⃣ Why are JWTs better for stateless systems?**

JWTs are better for stateless systems because all authentication information is stored in the token and sent with each request.

This allows any server instance to validate the token without needing shared session storage.

As a result, JWTs align with stateless architecture and enable easier horizontal scaling.

### **3️⃣ What is one drawback of JWT compared to sessions?**

A major drawback of JWT is that tokens are difficult to revoke once issued.

Since the server does not store session state, it cannot easily invalidate a token before its expiration.

As a result, if a token is compromised, it may remain valid until it expires, posing a security risk.
