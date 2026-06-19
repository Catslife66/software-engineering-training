# HTTP / HTTPS

Engineer Vocabulary

```
stateless
request context
session
horizontal scaling
server-side state
client state
```

## HTTP

HTTP = HyperText Transfer Protocol

It defines how data is transferred between client and server.

HTTP is a stateless protocol, meaning each request contains all the information required for processing. The server does not automatically retain request context between requests.

This property simplifies horizontal scaling because any server instance can handle any incoming request without relying on local server state.

### Request-Response Model

HTTP works like this:

```
Client → sends request
Server → sends response
```

Example:

```
Browser → GET /users

GET /users HTTP/1.1
Host: api.example.com


Server → returns user data

HTTP/1.1 200 OK
Content-Type: application/json
Body:
[
  { "id": 1, "name": "Alice" }
]
```

Then the connection can close.

### HTTP Methods

| Method | Meaning     |
| ------ | ----------- |
| GET    | read data   |
| POST   | create data |
| PUT    | update data |
| DELETE | remove data |

HTTP methods define the intent of the request, allowing the server to correctly interpret and handle the operation.

Example:

```
GET /users
POST /users
DELETE /users/1
```

This is the foundation of REST APIs.

### HTTP is Stateless

> Each request is independent

The server does NOT remember previous requests automatically.

Example:

```
Request 1 → login
Request 2 → get profile
```

The server does not "remember" you unless you send:

- cookies
- tokens (JWT)

```
HTTP is considered stateless because each request contains all the information required for processing, and the server does not automatically retain request context between requests.

Since requests are independent, servers do not need to rely on local server-side state. This allows a load balancer to distribute incoming requests across multiple server instances.

As a result, statelessness simplifies horizontal scaling because any server can process any request without requiring knowledge of previous requests.
```

## HTTPS

HTTPS = HTTP + encryption

It uses **TLS (Transport Layer Security)**

HTTPS is HTTP over TLS, which encrypts the communication to ensure:

- confidentiality (no one can read it)
- integrity (data cannot be modified)
- authenticity (you are talking to the real server) -> it prevents man-in-the-middle attacks.

Real Example

When you visit: https://yourapp.com

The flow is:

```
User
 ↓
DNS lookup
 ↓
TCP connection
 ↓
HTTPS (secure channel)
 ↓
HTTP request
 ↓
Server response
```

## Mental Model

HTTP = conversation rules

HTTPS = secure conversation
