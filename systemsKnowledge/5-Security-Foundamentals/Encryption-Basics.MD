# Encryption Basics (HTTPS and Data Protection)

How data is protected during transmission?

**Problem**

When data travels over the internet:

```
passwords
tokens
API data
```

👉 can be intercepted:

attacker → reads or modifies data ❌

**Solution — Encryption**

Encryption transforms:

```
plaintext → ciphertext
```

Only someone with the key can read it.

## Two Types of Encryption

**1. Symmetric Encryption**

```
same key for encrypt + decrypt
```

Example:

```
Client ↔ Server share secret key
```

Pros:

- fast
- efficient

Cons:

- key distribution problem

**2. Asymmetric Encryption**

```
public key + private key
```

- Public key → encrypt
- Private key → decrypt

## Real Use Case — HTTPS

When you visit: https://example.com

Flow:

```
1. Client gets server's public key
2. Client generates session key
3. Client encrypts session key using public key
4. Server decrypts using private key
5. Both use symmetric encryption for communication
```

## Why Combine Both?

```
Asymmetric → secure key exchange
Symmetric → fast data transfer
```

## What HTTPS Protects

```
✔ confidentiality (no one reads data)
✔ integrity (data not modified)
✔ authenticity (real server)
```

Without HTTPS: login password → visible ❌

With HTTPS: login password → encrypted ✅

## Mental model

```
HTTPS handshake:
Asymmetric → securely share key
↓
Symmetric → fast encrypted communication
```

## CHECKPOINT

**1️⃣ Why do we use asymmetric encryption first in HTTPS?**

```
Asymmetric encryption is used first in HTTPS to securely exchange a shared symmetric key between the client and the server.

Since the client and server do not share a secret initially, asymmetric encryption allows the client to encrypt a session key using the server’s public key.

As a result, both parties can safely establish a shared key for further communication.
```

**2️⃣ Why not use asymmetric encryption for everything?**

```
Asymmetric encryption is computationally expensive and slower than symmetric encryption.

Using it for all data transmission would significantly reduce performance and increase resource usage.

As a result, it is only used for the initial key exchange, after which faster symmetric encryption is used for the rest of the communication.
```

**3️⃣ What are the three main things HTTPS protects?**

```
HTTPS provides three main protections:

Confidentiality: Data is encrypted so it cannot be read by attackers.

Integrity: Data cannot be modified during transmission without detection.

Authenticity: The client can verify it is communicating with the legitimate server using certificates.
```

**In one flow, can you describe what happens when a user logs in to a website securely?**

Include:

hashing
HTTPS
authentication (session or JWT)
request flow

```
1. Client initiates HTTPS connection (TLS handshake)
   → server provides its certificate (with public key)

2. Client verifies the server identity
   → generates a session key
   → encrypts it using the server’s public key

3. Server decrypts the session key using its private key
   → secure channel (symmetric encryption) is established

4. Client sends login request over HTTPS
   → username + password (NOT hashed on client side)

5. Server receives request
   → hashes the password (with salt)
   → compares with stored hash in database

6. If valid, server generates authentication (session or JWT)

7. Server sends token/session ID back to client

8. Client stores token (cookie/local storage)

9. Client sends token with future requests

10. Server verifies token → authenticates user
```
