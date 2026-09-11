# HTTP

TCP answered:

> How can bytes move reliably and in order between two endpoints?

HTTP answers a different question:

> What do those bytes mean to a web application?

This distinction is fundamental:

```
TCP
→ transport

HTTP
→ application protocol
```

HTTP gives structure and semantics to communication between clients and servers.

## 1. Mental Model

Imagine a browser wants a product:

```
Browser
   ↓
"Give me product 42"
   ↓
Backend
```

TCP can transport the bytes, but TCP doesn't understand:

```
product
GET
404
JSON
authentication header
```

Those are application-level concepts.

HTTP defines a common language for expressing them.

Conceptually:

```
Client
  │
  │ HTTP Request
  │
  ▼
Server
  │
  │ HTTP Response
  │
  ▼
Client
```

For example:

```
GET /products/42 HTTP/1.1
Host: shop.example
Accept: application/json
```

Response:

```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 42,
  "name": "Keyboard"
}
```

HTTP gives meaning to the communication:

```
method
+
resource
+
headers
+
body
+
status
```

---

**HTTP sits above transport**

A simplified stack:

```
Application
    ↓
HTTP
    ↓
TCP
    ↓
IP / Network
```

For traditional HTTP/1.1 and HTTP/2, TCP commonly provides transport.

HTTP/3 instead uses QUIC.

So:

```
HTTP
→ application semantics

Transport
→ moves the communication
```

---

**Request → response**

The basic HTTP interaction is:

```
Client
↓
Request
↓
Server processes request
↓
Response
↓
Client
```

A request communicates intent.

For example:

```
GET /products/42
```

means approximately:

> Retrieve a representation of product 42.

While:

```
DELETE /products/42
```

expresses a different intent.

This brings us to something very important:

> HTTP methods are not just different words for calling endpoints. They communicate semantics.

---

**Statelessness**

You've encountered this before.

HTTP is described as a stateless protocol.

That does not mean:

```
HTTP applications cannot have state.
```

Obviously your application has:

```
users
orders
shopping carts
sessions
database records
```

Instead, the important mental model is:

**Each HTTP request should contain enough information for the server to understand and process that request without relying on conversational state implicitly maintained by HTTP itself from the previous request.**

Suppose:

```
Request 1
→ GET /products

Request 2
→ POST /orders
```

HTTP itself doesn't inherently say:

Request 2 belongs to whoever made Request 1.

Applications add mechanisms such as:

```
session cookie
authentication token
```

to establish identity/context.

So:

```
HTTP statelessness
≠
application has no state
```

Keep that distinction.

## 2. Engineer Vocabulary

**Request**

A message from client to server expressing an operation.

Typically contains some combination of:

```
method
URL/path
headers
body
```

---

**Response**

The server's HTTP result.

Typically:

```
status code
headers
body
```

---

**Resource**

Something exposed through the HTTP interface.

Examples:

```
/users/42
/orders/991
/products/10
```

Think in terms of domain resources rather than simply Java methods.

---

**HTTP method**

Communicates the intended operation.

Important methods:

```
GET
POST
PUT
PATCH
DELETE
```

At our level:

```
GET
→ retrieve

POST
→ submit/create/process

PUT
→ replace/update a resource representation

PATCH
→ partially modify

DELETE
→ remove
```

Real APIs can have nuances, but this is the useful starting model.

---

**Safe method**

In HTTP terminology, a safe method is intended to be read-only from the client's requested semantics.

GET should not be designed to perform something like:

```
GET /charge-customer
```

even if Java technically allows you to write such an endpoint.

Why?

Because infrastructure and clients reason about HTTP semantics.

---

**Idempotent method**

An operation is idempotent when repeating the same intended operation has the same intended effect as performing it once.

Conceptually:

```
DELETE /products/42
```

Once:

```
product 42 absent
```

Five times:

```
product 42 still absent
```

Compare:

```
POST /payments
```

If implemented naively:

```
request 1 → charge £50
retry     → charge another £50
```

not idempotent.

Important:

> HTTP method semantics help communicate expectations, but the application implementation must still preserve the required business behaviour.

---

**Status code**

Communicates the outcome at the HTTP level.

Broad families:

```
2xx → success
3xx → redirection
4xx → client-side/request problem
5xx → server-side problem
```

Common examples:

```
200 OK
201 Created
204 No Content

400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict

500 Internal Server Error
503 Service Unavailable
```

Don't obsess over memorising every code.

The engineering principle is:

> Status codes are part of your API contract.

---

**Headers**

Metadata associated with the request or response.

Examples:

```
Content-Type
Accept
Authorization
Cache-Control
```

---

**Body / payload**

The application data being transferred.

Often:

```
{
  "productId": 42,
  "quantity": 2
}
```

---

**API contract**

The agreed interface between systems:

```
request structure
response structure
semantics
status codes
error behaviour
```

Changing an API contract can break consumers.

---

**Statelessness**

Requests are treated as independent at the HTTP protocol level; required application context must be supplied or resolved explicitly.

---

**Connection reuse**

Multiple HTTP interactions may reuse underlying transport connections rather than establishing a fresh connection every time.

This reduces connection-establishment overhead.

## 3. Engineer Explanation

A strong engineer explanation:

```
HTTP is an application-layer request-response protocol that defines semantics for communication between clients and servers. Requests describe an intended operation using methods, resources, headers and optional bodies, while responses communicate outcomes through status codes, headers and bodies. HTTP is stateless at the protocol level, so application state and identity must be managed explicitly through mechanisms such as databases, sessions or tokens.
```

Now the architecture-review version:

```
HTTP is not merely a transport for JSON. Its methods, status codes, caching semantics and idempotency expectations form part of the contract between distributed components. Good HTTP API design communicates intent and failure clearly enough that clients, servers and infrastructure can behave predictably.
```

That's the explanation I want us eventually to internalise.

## 4. Trade-offs

HTTP gives us a standardized application protocol.

That makes systems easier to integrate:

```
Browser
Mobile app
Backend service
Third-party client
       ↓
      HTTP
```

But abstraction has costs.

---

**Human-friendly structure vs overhead**

HTTP carries metadata:

```
method
path
headers
status
content type
cookies
etc.
```

That's more overhead than simply sending a tiny custom binary message over the network.

But we gain:

```
standard semantics
interoperability
tooling
proxies
caching
debuggability
broad ecosystem support
```

So:

> Protocol overhead buys standardisation and interoperability.

---

**Statelessness vs repeated context**

Stateless request handling can make scaling easier.

Imagine:

```
Request 1 → Server A
Request 2 → Server B
Request 3 → Server C
```

If every required piece of conversational state lived only inside Server A's memory:

```
Request 2 → Server B
↓
"Who are you?"
```

Scaling becomes harder.

A stateless application tier can instead resolve state from shared/external sources:

```
Request
↓
authentication/context
↓
any server
↓
shared database/cache/etc.
```

Benefit:

```
easier horizontal scaling
```

Cost:

```
state must be carried or retrieved somewhere
```

Notice the precise wording:

> Stateless application servers can simplify horizontal scaling.

Not:

HTTP automatically makes my application stateless.

---

**Rich API calls vs network cost**

A local Java method:

```
productService.findById(42);
```

is fundamentally different from:

```
GET https://service/products/42
```

The remote call introduces:

```
network latency
serialization
connection behaviour
timeouts
partial failure
remote availability
```

This is one of the most important architecture habits:

> A remote call is not just a slower local method call.

It has fundamentally different failure modes.

## 5. Failure Modes

**Wrong status semantics**

Suppose:

```
HTTP/1.1 200 OK

{
  "success": false,
  "error": "Database unavailable"
}
```

Technically possible.

But you've made the API harder for clients and infrastructure to reason about.

The HTTP layer says:

```
success
```

while the application body says:

```
failure
```

Better API contracts align protocol semantics with application outcomes where appropriate.

---

**Unsafe GET operations**

Imagine:

```
GET /orders/42/cancel
```

This changes business state.

Why is that dangerous?

Because clients, crawlers, caches and infrastructure may treat GET as safe/read-only.

The URL might accidentally be fetched.

So:

> HTTP semantics affect system behaviour beyond your controller code.

---

**Blind retries**

This should now look very familiar.

```
POST /payments
↓
payment succeeds
↓
response lost
↓
client timeout
↓
retry POST
↓
???
```

If the operation isn't designed for safe retries:

```
duplicate charge
```

Again:

```
HTTP request failed from caller's perspective
≠
business operation definitely did not happen
```

Transport and application uncertainty remain.

---

**Long-running request path**

Suppose:

```
POST /orders
↓
reserve inventory       200ms
↓
charge payment          500ms
↓
send email              4sec
↓
analytics               2sec
↓
response
```

The customer waits for everything.

But our Business Invariants training tells us:

```
Critical path
→ business correctness

Side effects
→ potentially asynchronous
```

Perhaps:

```
POST /orders
↓
critical business operations
↓
201 Created
```

while:

```
email
analytics
```

happen asynchronously.

This can reduce response latency — but introduces messaging/reliability complexity.

Again:

> Architecture is trade-offs.

---

**Timeout propagation**

Imagine:

```
Browser
↓
Service A
↓
Service B
↓
Service C
↓
Database
```

If the database becomes extremely slow:

```
Database slow
↓
C waits
↓
B waits for C
↓
A waits for B
↓
Browser waits for A
```

One slow dependency has propagated latency through the entire request chain.

This is a **cascading failure risk**.

Timeouts and bounded resource usage help contain this, but must be designed across the request path.

---

**Large payloads**

Suppose an endpoint returns:

```
2 GB JSON response
```

Even if everything is logically correct:

```
serialization cost
memory pressure
network bandwidth
latency
client processing
```

become architectural concerns.

API design includes **how much data crosses the boundary**, not just endpoint naming.

## 6. Real Systems

**Spring Boot request**

Here's a familiar architecture:

```
Browser
↓
POST /api/orders
↓
Spring Controller
↓
OrderService
↓
Repository
↓
PostgreSQL
```

The controller might look conceptually like:

```
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(
        @RequestBody CreateOrderRequest request) {

    Order order = orderService.create(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toResponse(order));
}
```

Systems interpretation:

```
HTTP
↓
defines external contract

Controller
↓
translates HTTP request into application call

Service
↓
enforces business rules

Repository
↓
persistence boundary
```

This distinction matters.

Your business logic shouldn't become:

```
if HTTP status ...
```

everywhere.

HTTP belongs primarily at the system boundary.

---

**Product API**

Consider:

```
GET /products/42
```

Possible outcomes:

```
product exists
→ 200 OK

product doesn't exist
→ 404 Not Found

database unavailable
→ 5xx response
```

These outcomes tell the client different things.

A 404 means:

```
The requested resource wasn't found.
```

A server failure means:

```
The server couldn't successfully fulfil the request.
```

Those distinctions influence client behaviour.

For example, retrying:

```
404
```

repeatedly probably doesn't help.

Retrying certain transient server failures might help.

Therefore:

> Error semantics influence resilience behaviour.

---

**Authentication**

Suppose:

```
GET /users/me
Authorization: Bearer ...
```

The request carries authentication information.

Server A can process it.

Next request reaches Server B:

```
Load Balancer
     ↓
Server B
```

Server B can also resolve authentication.

This helps explain why stateless request processing works well with:

```
load balancing
horizontal scaling
```

We'll revisit that in Phase C.

## 7. Communication Training

Now let's use HTTP semantics to reason about an API design.

A developer creates these endpoints:

```
GET /api/orders/42/cancel
```

Cancels order 42.

```
POST /api/payments
{
    "orderId": 42,
    "amount": 100
}
```

Charges the customer.

```
GET /api/products/10
```

Returns product 10.

During an incident, clients automatically retry requests when they experience timeouts.

You're reviewing the API.

Explain:

```
1. What concerns you about using GET to cancel an order?
GET is defined as a safe method, so clients and infrastructure should be able to invoke it without requesting a state-changing operation.

2. Is retrying GET /api/products/10 generally less dangerous than blindly retrying POST /api/payments? Why?
GET is generally safer to retry from a business-side-effect perspective, but uncontrolled retries can still create operational load.

3. Suppose the payment service charges the customer successfully but the HTTP response is lost. What does the timeout tell the client?
A timeout tells us only the caller did not receive the expected response within the configured time limit.

4. What business problem can a blind payment retry create?
recharging for the same order.

5. What property would we like the payment operation to have so retries can be handled safely?
The payment operation should be idempotent. For example, the client can provide an idempotency key representing the logical payment intent, allowing retries to return the existing result rather than creating another charge.

6. Why is an HTTP call fundamentally different from calling an ordinary local Java method?
A local call might fail with an exception.

A remote call introduces a particularly nasty possibility:
Caller:
"I don't know what happened."
Remote service:
"I successfully completed it."

So one of the most important rules in distributed systems is:
A remote call is not just a slower local method call.
It has fundamentally different failure semantics.

7. A developer says "HTTP is stateless, so our application shouldn't store user sessions or orders.", what's wrong with that statement?
The server doesn't depend on HTTP itself implicitly remembering the previous request's conversational state. The request provides or identifies the context needed to resolve its processing.
```

```
Using GET to cancel an order violates the expected semantics of a safe HTTP method. Although it's technically possible to implement in Java, clients and infrastructure may assume GET requests don't perform requested state-changing operations.

Retrying a product GET is generally safer than blindly retrying a payment because the GET is normally read-only, whereas repeating a non-idempotent payment request could create another charge. However, safe requests still consume system resources, so uncontrolled GET retries can create operational load.

If a payment request times out, the client only knows that it didn't receive the expected response within the configured time. It cannot conclude that the payment failed. The remote operation may already have succeeded, leaving the business outcome ambiguous from the caller's perspective. Payment operations should therefore support safe retries—for example, through idempotency keys.

A remote HTTP call is fundamentally different from a local Java method call because it crosses a network boundary and introduces latency, serialization, timeouts, remote availability and partial failures. A remote operation may succeed even when the caller observes failure.

Finally, HTTP statelessness doesn't mean the application cannot maintain state. It means HTTP itself doesn't implicitly maintain conversational application state between requests. Business state can still live in databases, caches and other services, while each request supplies or identifies the context required for processing.
```

## 8. Technology Spotlight — HTTP Versions

You should recognize three major generations:

```
HTTP/1.1
HTTP/2
HTTP/3
```

You do not need their implementation details yet.

**HTTP/1.1**

Still widely understood and used. Supports persistent connections rather than requiring a new TCP connection for every request.

**HTTP/2**

Adds features including multiplexing multiple HTTP streams over a connection and more efficient header representation.

Conceptually:

```
one connection

├── request/response A
├── request/response B
└── request/response C
```

**HTTP/3**

Runs HTTP over QUIC rather than TCP.

Recall:

```
HTTP/1.1 ─┐
           ├→ commonly TCP
HTTP/2   ──┘

HTTP/3
↓
QUIC
↓
UDP
```

The important lesson isn't memorising versions.

It's recognizing our layered architecture:

```
HTTP
→ application semantics

QUIC / TCP
→ transport behaviour

IP
→ network delivery
```

Different layers solve different problems.

---

# Handbook Page — HTTP

## Mental Model

HTTP is an application-layer request-response protocol that gives semantic structure to communication between clients and servers.

```
Client
  ↓
HTTP Request
  ↓
Server
  ↓
HTTP Response
  ↓
Client
```

A request communicates:

```
method
resource
headers
body
```

A response communicates:

```
status
headers
body
```

## Protocol Stack

```
Application
     ↓
HTTP
     ↓
Transport
(TCP or QUIC)
     ↓
Network
```

Remember:

```
TCP
→ reliable transport abstraction

HTTP
→ application communication semantics
```

## Key Vocabulary

```
request
response
resource
method
safe
idempotent
status code
headers
body / payload
API contract
statelessness
connection reuse
latency
remote call
```

## Method Mental Model

```
GET
→ retrieve

POST
→ submit/create/process

PUT
→ replace/update

PATCH
→ partially modify

DELETE
→ remove
```

But methods communicate semantics, not merely CRUD syntax.

## Status Mental Model

```
2xx
→ successful outcome

3xx
→ redirection

4xx
→ client/request-side issue

5xx
→ server-side failure
```

Choose status codes as part of the API contract.

## Statelessness

```
HTTP is stateless
        ≠
application has no state
```

Instead:

> HTTP does not inherently maintain conversational application state between independent requests.

Application state may live in:

```
database
cache
session store
token
other services
```

## Engineering Principles

1. HTTP defines application semantics; TCP/QUIC provides transport.

2. HTTP methods communicate intent and have behavioural semantics.

3. Status codes are part of the API contract, not decorative metadata.

4. A remote HTTP call has latency and partial-failure modes that a local method call does not.

5. A timeout tells us that the caller stopped waiting — not necessarily that the business operation failed.

6. Retries must consider idempotency and business invariants.

7. Stateless application servers can simplify horizontal scaling, but application state still has to live somewhere.

8. API design affects reliability, scalability and client behaviour—not merely code organisation.

## Failure Thinking

When reviewing an HTTP interaction, ask:

```
What operation is this request expressing?

Is the method semantically appropriate?

Is it safe?

Is it idempotent?

What does each status code mean?

What happens on timeout?

Can the client retry safely?

Could the operation already have succeeded?

What business invariant are we protecting?

How much data crosses the network?

What dependency is the request waiting for?

Can downstream latency propagate upstream?
```

## Engineer Explanation

```
HTTP is an application-layer request-response protocol that defines semantics for communication between distributed components. Methods express intent, status codes communicate outcomes, and headers and bodies carry metadata and application data. HTTP is stateless at the protocol level, but applications still maintain business state explicitly. Because HTTP calls cross network boundaries, engineers must design for latency, timeouts, partial failures and retries rather than treating remote calls like ordinary local method calls.
```
