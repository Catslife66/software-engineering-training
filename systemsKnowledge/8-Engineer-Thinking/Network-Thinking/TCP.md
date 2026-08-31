# TCP

## 1. Mental Model

Imagine your Spring Boot application needs information from another service:

```
Order Service                     Payment Service

     │                                  │
     │────── network communication ────→│
     │                                  │
     │←──────── response ───────────────│
```

The network between them is **not inherently reliable**.

Data can:

```
be lost
arrive late
arrive out of order
arrive more than once
```

So applications need some mechanism for communicating despite that uncertainty.

TCP provides a useful abstraction:

> **TCP gives applications a reliable, ordered byte stream between two endpoints over an unreliable network.**

That's the central mental model.

Your application can largely reason:

```
write bytes
      ↓
TCP
      ↓
network
      ↓
TCP
      ↓
read bytes
```

while TCP handles much of the machinery required for reliable delivery.

**TCP reliability is not the same as business-operation reliability**.

---

### Establishing a connection

Before application data normally flows over TCP, the two endpoints establish a connection.

You've previously seen:

```
SYN
SYN-ACK
ACK
```

The three-way handshake.

Conceptually:

```
Client                         Server

  │──── SYN ──────────────────→│
  │                            │
  │←── SYN-ACK ────────────────│
  │                            │
  │──── ACK ──────────────────→│
  │                            │
       connection established
```

**Connection establishment requires network communication before useful application data can flow.**

That has a latency cost.

---

### Reliable delivery

Suppose the sender transmits:

```
A B C D
```

Packet containing C disappears.

TCP can detect missing data and arrange retransmission.

Conceptually:

```
send:
A B C D

network:
A B ✕ D

TCP:
"C is missing"

retransmit:
C
```

The receiving application eventually sees the ordered stream:

```
A B C D
```

This is a huge convenience for application developers.

---

### Ordered delivery

Suppose network packets arrive:

```
A
C
B
D
```

Your application generally shouldn't have to manually reconstruct the original ordering.

TCP does that work.

Application sees:

```
A B C D
```

Again:

> TCP hides substantial network complexity behind a reliable ordered-stream abstraction.

---

### Reliability creates waiting

Here's the engineering part.

Suppose:

```
A B C D
```

and B is delayed.

Even if C and D have arrived, TCP must preserve ordering.

Conceptually:

```
received:

A ✓
B ?
C ✓
D ✓

application:
A ...
   ↑
wait for B
```

Later:

```
B arrives/retransmitted
↓
A B C D
↓
application continues
```

Reliability and ordering therefore have costs.

That leads directly into our trade-offs.

## 2. Engineer Vocabulary

**Connection-oriented**

TCP establishes state between communicating endpoints before normal communication.

---

**Three-way handshake**

The connection-establishment process:

```
SYN
SYN-ACK
ACK
```

---

**Reliable delivery**

TCP detects certain delivery failures and retransmits data as necessary.

---

**Ordered delivery**

The application receives the byte stream in the correct order.

---

**Byte stream**

TCP exposes data as a continuous sequence of bytes.

An important consequence:

> TCP does not inherently preserve your application's message boundaries.

If you conceptually send:

```
"HELLO"
"WORLD"
```

TCP fundamentally sees bytes, not two business messages.

Higher-level protocols such as HTTP define structure on top.

---

**Acknowledgement — ACK**

Confirmation used by TCP as part of its reliability machinery.

---

**Retransmission**

Sending data again when TCP determines that previous transmission was not successfully delivered.

---

**Round-trip time — RTT**

The time required for communication to travel to another endpoint and for a response/acknowledgement to return.

Often discussed as:

```
network RTT
```

---

**Latency**

Time taken for an operation or communication to complete.

---

**Throughput**

Amount of useful data/work transferred over a period of time.

Latency and throughput are different.

A connection can have:

```
high throughput
+
high latency
```

---

**Packet loss**

Network data fails to reach its destination.

---

**Timeout**

A limit on how long an operation is allowed to wait before being treated as failed.

---

**Connection**

State maintained by endpoints to support TCP communication.

---

**Socket**

At our level, think:

> A socket is an operating-system abstraction an application uses to communicate over a network connection.

We'll keep the low-level socket API out of this handbook.

## 3. Engineer Explanation

A strong backend-engineer explanation is:

```
TCP is a connection-oriented transport protocol that provides applications with a reliable, ordered byte stream. It handles mechanisms such as acknowledgements, retransmission and ordering so applications don't have to manage packet loss and reordering directly. That reliability has costs, including connection establishment, protocol overhead and additional latency when data is lost or delayed.
```

But there's another explanation I want you to know:

> **TCP provides transport-level reliability between endpoints; it does not guarantee that a business operation completed successfully.**

This distinction is extremely important.

Suppose:

```
Order Service
      ↓
TCP
      ↓
Payment Service
```

Order Service sends:

```
charge customer £50
```

Payment Service receives it and successfully charges the card.

Then:

```
Payment Service
↓
sends response

NETWORK FAILURE

Order Service
↓
times out
```

What does Order Service know?

Maybe:

```
payment failed
```

Or maybe:

```
payment succeeded
but response was lost
```

TCP cannot magically resolve that business ambiguity after the connection fails.

This is why concepts we've already previewed become necessary:

```
timeouts
retries
idempotency
business state
source of truth
```

So:

> Reliable transport does not imply exactly-once business execution.

That's one of the most important ideas in Network Thinking.

## 4. Trade-offs

TCP gives us substantial guarantees:

```
connection
reliability
ordering
retransmission
flow control
congestion control
```

Those guarantees aren't free.

---

**Reliability vs latency**

When data disappears:

```
packet loss
↓
detect missing data
↓
retransmit
↓
wait
↓
application receives correct stream
```

Benefit:

```
correct ordered delivery
```

Cost:

```
additional latency
```

This is a recurring engineering pattern:

> Stronger guarantees often require additional coordination, state or waiting.

You've already seen the same principle with synchronization and consistency.

---

**Connection state**

TCP maintains connection-related state.

A server handling many connections therefore consumes resources.

Conceptually:

```
10 connections
→ manageable state

1,000,000 connections
→ significant system consideration
```

We'll eventually connect this to load balancers, proxies and infrastructure.

---

**Long-lived vs new connections**

Suppose every API request did:

```
establish TCP connection
↓
send request
↓
receive response
↓
close connection
```

Repeated connection establishment adds overhead.

Reusing connections can avoid some of that work:

```
establish connection
↓
request
response
↓
request
response
↓
request
response
```

This will become relevant when we study HTTP connection reuse.

## 5. Failure Modes

Networking becomes interesting when things don't work.

**Packet loss**

```
sender
↓
network
↓
packet disappears
```

TCP may retransmit.

Result:

```
reliability maintained
but
latency increases
```

---

**Slow network**

Nothing necessarily fails.

It just takes longer.

This matters because:

```
Thread
↓
network request
↓
BLOCKED / WAITING
```

Remember Phase A?

Slow networks can therefore affect:

```
network latency
↓
longer I/O waiting
↓
worker threads occupied longer
↓
thread-pool pressure
↓
application latency
```

A network problem can become an application-capacity problem.

That's systems thinking.

---

**Connection failure**

Connections can disappear because of:

```
machine crash
process crash
network interruption
load balancer behaviour
timeout
```

The application must decide what to do.

Maybe:

```
retry
```

But retries create another danger:

```
original operation actually succeeded
↓
response lost
↓
client retries
↓
operation executes twice
```

Hello again:

> Idempotency.

---

**Timeout configuration**

Suppose a dependency normally responds in:

```
100 ms
```

but your application waits:

```
5 minutes
```

when it becomes unhealthy.

Your worker threads could accumulate:

```
slow dependency
↓
threads wait
↓
more requests arrive
↓
more threads wait
↓
thread-pool saturation
↓
your service becomes unhealthy too
```

This is how a dependency failure can propagate upstream.

Timeouts therefore aren't merely networking settings.

> **Timeouts are part of failure containment**.

---

**Retry storm**

Suppose 10,000 clients encounter a temporary network failure.

They all immediately retry:

```
failure
↓
10,000 retries
↓
dependency already struggling
↓
even more load
↓
more failures
↓
more retries
```

This can amplify an incident.

Later we'll study:

```
backoff
jitter
retry budgets
```

For now:

> **Retries improve resilience to transient failures, but uncontrolled retries can amplify overload.**

## 6. Real Systems

Consider your backend:

```
Browser
   ↓
Internet
   ↓
Spring Boot API
   ↓
PostgreSQL
```

There may be multiple TCP connections involved:

```
Browser
   │
   │ TCP
   ↓
Backend
   │
   │ TCP
   ↓
PostgreSQL
```

Now imagine PostgreSQL becomes reachable but extremely slow.

The network connection may still technically work.

But:

```
database response takes 10 seconds
↓
request thread waits
↓
more HTTP requests arrive
↓
more workers wait
↓
thread pool becomes saturated
```

So a production incident might initially appear as:

"Our web server is slow."

But the root cause could be:

```
database
network
downstream API
lock contention
CPU
```

This is why our recurring question is:

> **Where is the bottleneck, and what is each part of the system waiting for?**

---

**Payment Request**

```
Order Service
↓
TCP connection
↓
Payment Service
```

Order Service sends:

```
Charge order 123
```

Payment succeeds.

Response disappears.

Order Service sees:

```
TIMEOUT
```

An inexperienced implementation:

```
timeout
↓
retry charge
```

Potential result:

```
customer charged twice
```

A more robust design asks:

```
What is the business invariant?

Is the operation idempotent?

Can we identify this operation uniquely?

Can we safely retry?

How do we reconcile uncertain outcomes?
```

Notice how Network Thinking is already connecting back to Business Invariants.

## 7. Communication Training

Now you're reviewing an order service.

Normal behaviour:

```
Payment API latency:       150 ms
Application CPU:           35%
Worker utilisation:        moderate
Response latency:          ~300 ms
```

During an incident:

```
Payment API latency:       8 seconds
Application CPU:           30%
Worker threads:            almost all occupied
Response latency:          10+ seconds
```

A teammate says:

> "The CPU isn't busy, so let's increase the worker thread pool."

Explain as the backend engineer:

```
1. Why can the worker pool be saturated while CPU utilisation remains low?
The worker pool is saturated by blocked I/O rather than CPU execution.

2. What is the likely bottleneck?
The payment API appears to be the constrained downstream dependency.

CPU = 30%
        ↓
CPU doesn't appear saturated

Payment API 150 ms → 8 seconds
        ↓
downstream latency dramatically increased

Workers almost all occupied
        ↓
requests waiting on dependency

3. How does this connect to blocking I/O?
Thread sends payment request
↓
network I/O
↓
thread cannot continue until response
↓
BLOCKED / WAITING

4. Why could adding more worker threads make the incident worse?
Suppose:
200 workers
↓
200 possible concurrent payment calls

and we change to:
500 workers
↓
potentially even more concurrent payment calls

We're increasing pressure on the component already struggling:
slow Payment API
↓
add application concurrency
↓
more payment requests
↓
more downstream pressure
↓
possibly even slower Payment API
↓
workers wait longer

5. What role could timeouts play in containing the failure?
Timeouts provide failure containment by preventing our resources from waiting indefinitely:
slow dependency
↓
timeout
↓
caller stops waiting
↓
application resource released
↓
dependency cannot indefinitely occupy every worker

6. Suppose a payment request times out. Why can't we safely assume the payment failed?
The outcome is ambiguous from the caller's perspective.
A timeout tells you about your observation of the operation, not necessarily its business outcome.

Suppose:
Order Service
     ↓
"charge order 123"
     ↓
Payment Service

Payment Service
     ↓
CHARGE SUCCEEDS
     ↓
response
     X ← network failure

Order Service
     ↓
TIMEOUT

The caller knows:
I did not receive a successful response.

It does not know:
The payment definitely failed.


7. Why can blindly retrying the payment be dangerous?
Blindly retrying a non-idempotent payment operation can execute the same business side effect more than once.
```

```
The worker pool can become saturated even when CPU utilisation remains low because worker threads may be blocked waiting for network I/O rather than consuming CPU. In this incident, CPU utilisation is only 30%, while payment API latency has increased from 150 ms to eight seconds and most workers are occupied. This suggests that the payment API is the constrained downstream dependency rather than the CPU.

Increasing the worker-thread pool is unlikely to solve the underlying problem and could increase concurrent pressure on an already degraded dependency. Appropriate timeouts can help contain the failure by preventing application workers from waiting indefinitely. However, a timeout only means that the caller stopped waiting; it does not prove that the remote operation failed.

For a payment operation, the charge may have succeeded even though its response was lost. Blindly retrying could therefore execute the side effect twice. Retry behaviour must consider the business invariant and whether the operation is idempotent.
```

## 8. Technology Spotlight — TCP sockets

You may encounter code or documentation talking about:

```
TCP sockets
server sockets
open connections
connection timeout
read timeout
socket timeout
```

At this stage, the important model is:

```
Application
↓
Socket API
↓
Operating System
↓
TCP
↓
Network
```

Your Spring Boot application normally doesn't manually implement TCP reliability.

Libraries, frameworks and the operating system handle most of this machinery.

As a software engineer, however, you still need to reason about:

```
connections
timeouts
latency
failure
resource usage
```

because those behaviours affect your application architecture.

---

# Handbook Page — TCP

## Mental Model

> TCP provides applications with a reliable, ordered byte stream between network endpoints over an unreliable network.

```
Application
     ↓
TCP
     ↓
Unreliable network
     ↓
TCP
     ↓
Application
```

TCP handles much of:

```
connection establishment
ordering
acknowledgements
retransmission
flow control
congestion control
```

## Key Vocabulary

```
TCP
connection-oriented
three-way handshake
SYN
ACK
byte stream
reliable delivery
ordered delivery
retransmission
packet loss
round-trip time (RTT)
latency
throughput
timeout
connection
socket
```

## Engineering Principles

1. Networks are unreliable; TCP provides useful transport-level reliability on top of them.

2. Reliability and ordering have costs in state, coordination and latency.

3. TCP reliability does not guarantee business-operation success.

4. A timeout means the caller stopped waiting; it does not necessarily tell us whether the remote operation executed.

5. Slow network I/O can consume application capacity even when CPU utilisation is low.

6. Connection failures and retries can create duplicate business operations unless the application is designed for them.

7. Timeouts help contain failures, while uncontrolled retries can amplify them.

## Failure Thinking

When network communication becomes slow or unreliable, ask:

```
Is the connection established?

Is there packet loss?

Has network latency increased?

Is the downstream service itself slow?

How many workers are blocked waiting?

What are our timeouts?

Are we retrying?

Are retries safe?

Is the operation idempotent?

Could the remote operation have succeeded
even though we never received the response?
```

## Engineer Explanation

```
TCP is a connection-oriented transport protocol that gives applications a reliable, ordered byte stream. It handles packet loss, retransmission and ordering, but those guarantees can introduce additional latency and state. Importantly, TCP only provides transport-level guarantees: application failures, timeouts and lost responses can still leave the business outcome uncertain. Backend systems therefore need appropriate timeout, retry and idempotency strategies on top of TCP.
```
