# Login System

**👉🏻 Design a simple login system**

Outline:

- components involved
- request flow
- how security is handled

```
Components: client, API server, database, optional session store
Flow: login → verify password → issue auth → verify on future requests
Security: HTTPS, hashed passwords, token/session validation
Tradeoff: sessions are easier to revoke, JWT scales more easily


1. Client sends login request over HTTPS
   { username, password }

2. Server receives the request

3. Server looks up the user record in the database

4. Server hashes the input password with the stored salt
   and compares the result with the stored password hash

5. If the credentials are valid, server creates authentication:
   - Session ID stored in session store, or
   - JWT returned to client

6. Server sends the session ID / JWT back to the client

7. Client stores it
   - Session ID usually in a cookie
   - JWT often in cookie or storage depending on design

8. Client includes it in future requests

9. Server verifies the session / JWT on each protected request

10. If valid, server treats the user as authenticated
```

## Why is HTTPS necessary in a login system even if passwords are hashed in the database?

```
HTTPS is necessary because password hashing only protects passwords at rest in the database, not during transmission.

When a client sends login credentials, HTTPS provides confidentiality so attackers cannot read the data, integrity so the data cannot be modified in transit without detection, and authenticity so the client can verify it is talking to the legitimate server.

As a result, HTTPS protects the communication channel, while hashing protects stored credentials. Both are needed for a secure login system.
```

### Security principle underneath it

A secure system needs protection across multiple layers:

```
Transport security
Storage security
Authentication security
```

## Why might storing JWT in localStorage be riskier than storing it in an HttpOnly cookie?

```
Storing JWT in localStorage is riskier because it is accessible by JavaScript running in the browser.

If the application has a cross-site scripting (XSS) vulnerability, an attacker can inject malicious scripts that read the token from localStorage and send it to themselves.

In contrast, HttpOnly cookies are not accessible via JavaScript, so even if an XSS attack occurs, the token cannot be easily stolen.

As a result, storing JWT in HttpOnly cookies reduces the risk of token theft through XSS attacks.
```

**localStorage**

```
✔ easy to use
✖ accessible via JavaScript
✖ vulnerable to XSS
```

**HttpOnly Cookie**

```
✔ not accessible via JavaScript
✔ safer against XSS
✖ automatically sent with requests (CSRF risk)
```

So in real systems:

```
HttpOnly cookie + CSRF protection
```

## If HttpOnly cookies are safer, why do some systems still use localStorage for JWT?

👉🏻 **Control in Frontend**

With localStorage:

```
const token = localStorage.getItem("token")

fetch("/api", {
  headers: {
    Authorization: `Bearer ${token}`
  }
})
```

👉 You control:

```
when token is sent
how it is sent
which API gets it
```

**Scenario: Frontend + API are separate**

```
Frontend: app.com
Backend API: api.com
```

Cookies:

```
✔ safer from XSS (HttpOnly)
✖ complex setup (CORS + SameSite)
✖ CSRF risk
```

localStorage:

```
✔ simple
✔ manually attach token
✔ works across domains easily
✖ XSS risk
```

**SPA (Single Page App) Architecture**

Modern apps (like your Next.js projects):

```
Frontend (React / Next.js)
↓
Backend API (separate service)
```

In this setup:

```
localStorage + Authorization header = simple and flexible
```

**Tradeoff**

```
localStorage → easier architecture, higher XSS risk
HttpOnly cookie → safer storage, more complex setup
```

## Why Cookies Become Complicated

Example:

```
Frontend: https://app.com
Backend API: https://api.com
```

This is cross-origin (different domain)

### What is CORS?

> CORS = Cross-Origin Resource Sharing

It controls:

```
Can frontend (app.com) talk to backend (api.com)?
```

Without CORS

```
Browser blocks the request ❌
```

With CORS (server config)

Server must allow it:

```
Access-Control-Allow-Origin: https://app.com
```

## Cookies + CORS (Where it gets tricky)

By default:

```
Browser does NOT send cookies in cross-origin requests
```

👉🏻 To allow cookies:

**Client must send:**

```
fetch("https://api.com", {
  credentials: "include"
})
```

**Server must allow:**

```
Access-Control-Allow-Credentials: true
```

👉 If either is missing:

```
Cookies won’t be sent ❌
```

## SameSite Cookie Attribute

This is a **security setting on cookies**.

It controls:

```
When cookies are allowed to be sent
```

### SameSite Options

**Strict**

```
Only sent for same-site requests
```

👉 Very secure, but:

```
Won’t work for app.com → api.com ❌
```

**Lax**

```
Sent for some cross-site cases (like navigation)
```

👉 Still limited.

**None**

```
Sent for cross-site requests
```

BUT requires:

```
Secure (HTTPS)
```

### Real Issue

To use cookies across domains:

```
samesite=None + secure=True
```

👉 Otherwise:

```
Cookie not sent ❌
```

### Why This Feels Complicated

Because cookies require:

```
CORS config
credentials config
SameSite config
HTTPS
```

Many moving parts.

## Why might cookies introduce CSRF risk but localStorage does not?

```
CSRF occurs because cookies are automatically included in requests by the browser.

This allows attackers to trick a user's browser into sending authenticated requests to a server without the user's consent.

As a result, malicious actions can be performed on behalf of the user without their knowledge.
```

### What is CSRF (Cross-Site Request Forgery)?

> An attacker tricks a user’s browser into making a request to your server without the user’s intention

Example:

User is logged in to: bank.com

They have a cookie: session=abc123

Attacker website

```
evil.com
```

Contains:

```
<img src="https://bank.com/transfer?amount=1000&to=attacker" />
```

**What happens?**

```
Browser sends request to bank.com
+ automatically includes cookie
```

Server sees:

```
valid session → thinks user is real ❌
```

👉 Money transferred.

❓ Why localStorage is NOT vulnerable the same way

Because:

```
localStorage is NOT automatically sent
```

So attacker cannot do:

```
<img src="...">  ❌ (no token attached)
```

They would need:

```
JavaScript access → XSS
```

### CSRF VS XSS

```
XSS → attacker runs JavaScript in your site
CSRF → attacker tricks browser to send request
```

### How Systems Defend Against CSRF

Common methods:

```
CSRF tokens
SameSite cookies
Double submit cookie pattern
```

## What security risk still exists, and how would you mitigate it?

If your app uses: HttpOnly cookies + SameSite=None

```
Using HttpOnly cookies with SameSite=None introduces CSRF risk because cookies are automatically sent with cross-site requests.

This allows attackers to trick a user’s browser into making authenticated requests without the user’s intention.

Setting Secure=True ensures cookies are only sent over HTTPS, but it does not prevent CSRF attacks.

To mitigate CSRF, the system should use protections such as CSRF tokens or SameSite restrictions to ensure that only legitimate requests are accepted.
```

### Correct Mitigations

**1️⃣ CSRF Token (Most Important)**

```
Server generates a secret token
Client must send it with each request
Server verifies it
```

👉 Attacker cannot guess this token.

**2️⃣ SameSite Cookie (When Possible)**

```
SameSite=Lax or Strict
```

👉 Prevents cross-site requests automatically

But:

```
SameSite=None → needed for cross-domain apps
→ so you lose this protection
```

**3️⃣ Additional Protections**

```
Check Origin / Referer headers
Use double-submit cookies
```

## Core insight

| Threat         | Protection            |
| -------------- | --------------------- |
| XSS            | HttpOnly              |
| CSRF           | CSRF token / SameSite |
| Network attack | HTTPS                 |
