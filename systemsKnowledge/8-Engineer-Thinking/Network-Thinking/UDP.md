# UDP

TCP gave us:

```
connection
reliable delivery
ordered delivery
retransmission
flow/congestion control
```

UDP asks a very different engineering question:

> **What if the application values low overhead and timeliness more than TCP-style reliable, ordered delivery?**

That makes UDP useful because sometimes late data is no longer valuable data.

## 1. Mental Model

Start with our TCP mental model.

Suppose an application sends:

```
A → B → C → D
```

TCP tries to provide the receiving application with:

```
A → B → C → D
```

even when the underlying network loses or reorders data.

Conceptually:

```
Network delivers:

A  C  D
   ↑
   B missing

TCP:
"Wait. We need B."

↓
retransmission / ordering
↓

Application receives:

A B C D
```

That's extremely useful when every piece matters.

Now imagine a live voice call.

You're saying:

```
"Hello, how are you?"
```

Some network data representing a tiny fraction of "Hello" disappears.

Would you rather:

Option A

```
wait for old missing audio
↓
delay newer audio
↓
conversation starts lagging
```

or:

Option B

```
accept tiny audio loss
↓
keep playing current audio
↓
conversation remains real-time
```

For many real-time applications, Option B is preferable.

This gives us the central UDP mental model:

> **UDP sends independent datagrams without establishing a TCP-style connection and without guaranteeing delivery or ordering.**

It gives applications fewer guarantees.

But therefore requires less transport-level coordination.

---

**Sending with UDP**

Conceptually:

```
Application
    ↓
Datagram A
    ↓
UDP
    ↓
Network
    ↓
UDP
    ↓
Application
```

Then:

```
Datagram B
Datagram C
Datagram D
```

Each is an independent unit.

Unlike TCP's byte stream, UDP preserves datagram/message boundaries.

If you send:

```
Datagram 1 = "HELLO"
Datagram 2 = "WORLD"
```

the protocol treats those as separate datagrams.

---

**What if something disappears?**

Suppose:

```
send:
A B C D

network:
A B ✕ D
```

At the basic UDP transport level:

```
A B D
```

may simply be what arrives.

UDP itself does not say:

```
"C disappeared. I'll retransmit it."
```

If the application requires recovery, the application or a higher-level protocol must provide it.

That's an important distinction.

---

**What if data arrives out of order?**

Suppose:

sent:

```
A B C D
```

Network delivers:

```
A C B D
```

UDP doesn't reconstruct:

```
A B C D
```

for you.

Again:

> **UDP does not provide TCP-style ordered delivery.**

---

**Connectionless**

TCP:

```
client
↓
three-way handshake
↓
connection
↓
application data
```

UDP does not require that TCP connection-establishment process before sending a datagram.

Conceptually:

```
Application
↓
send datagram
↓
Network
```

That's why UDP is described as connectionless.

Be careful with this phrase.

It doesn't mean:

"There is no communication between two machines."

It means UDP doesn't maintain the same kind of transport connection state and handshake that TCP does.

## 2. Engineer Vocabulary

**Datagram**

A self-contained unit of data sent using UDP.

This is one of the most important vocabulary differences:

```
TCP → byte stream

UDP → datagrams
```

---

**Connectionless**

UDP does not require TCP-style connection establishment before sending data.

---

**Best-effort delivery**

A useful conceptual phrase:

> UDP sends data without guaranteeing that it will arrive.

But don't interpret "best effort" as careless.

The network still attempts delivery. The point is that the protocol does not provide TCP's reliability guarantees.

---

**Packet/datagram loss**

A sent datagram never reaches the receiver.

---

**Out-of-order delivery**

Datagrams arrive in a different order from the order in which they were sent.

---

**Duplicate delivery**

Network behaviour can also result in duplicate packets being observed.

Applications shouldn't build a model that assumes:

```
exactly once
```

merely because UDP was used.

---

**Message boundary**

UDP preserves individual datagram boundaries.

Conceptually:

```
send(A)
send(B)
≠
one continuous byte stream
```

---

**Low latency**

A common reason UDP is selected is that applications may prefer timely delivery without waiting for TCP-style retransmission/ordering behaviour.

But here's an important precision:

> UDP does not magically guarantee low latency.

The network can still be slow or congested.

UDP simply provides fewer transport-level mechanisms that can introduce waiting.

---

**Packet loss tolerance**

Some applications can tolerate losing small amounts of data.

This is a business/application requirement, not a property that UDP decides for you.

---

**Real-time traffic**

Communication where timeliness is particularly important.

Examples:

```
voice
video
interactive gaming
```

## 3. Engineer Explanation

A good engineer explanation is:

```
UDP is a connectionless transport protocol that sends independent datagrams without guaranteeing delivery, ordering or retransmission. Because it provides fewer transport-level guarantees than TCP, it has less protocol coordination and is useful for workloads where timeliness or low overhead matters more than recovering every lost packet.
```

Then add the important architectural qualification:

```
Using UDP doesn't mean reliability is impossible. It means any required reliability, ordering or recovery must be implemented by the application or a higher-level protocol rather than being provided automatically by UDP.
```

That second sentence prevents a common beginner misconception:

```
TCP = reliable
UDP = unreliable garbage
```

That's not useful engineering thinking.

Instead:

```
What guarantees does the application need?

Where should those guarantees live?

What do those guarantees cost?
```

## 4. Trade-offs

Here's the heart of TCP vs UDP.

**TCP**

We want:

```
delivery reliability
+
ordering
+
retransmission
+
connection state
```

Benefit:

```
application gets strong transport abstraction
```

Cost:

```
more coordination
state
recovery behaviour
potential waiting
```

---

**UDP**

We accept:

```
possible loss
possible reordering
no built-in retransmission
```

Benefit:

```
less transport-level coordination
lower protocol overhead
application controls recovery strategy
```

Cost:

```
fewer guarantees
more application responsibility
```

So don't memorize:

TCP is slow; UDP is fast.

That's too crude.

Use:

> TCP and UDP provide different transport guarantees, and the appropriate choice depends on the application's requirements.

---

**Reliability vs timeliness**

Imagine live audio.

Packet 42 contains audio for:

```
10:00:00.100
```

It disappears.

The conversation has already progressed to:

```
10:00:00.600
```

Recovering packet 42 later may be pointless.

The user needed that audio then, not now.

So:

```
Perfect delivery
but late
```

may be less useful than:

```
slightly incomplete
but current
```

This gives us a powerful engineering principle:

> Correctness sometimes includes timeliness.

A system isn't necessarily better merely because it eventually delivers every piece of information.

## 5. Failure Modes

UDP's simplicity pushes responsibility upward.

**Packet loss**

```
Sender
↓
Datagram
↓
✕
```

The application must decide:

```
Ignore it?

Retry?

Request missing data?

Use redundancy?

Accept degraded quality?
```

There is no universal answer.

---

**Out-of-order data**

Suppose game updates say:

```
1: player at x=10
2: player at x=20
3: player at x=30
```

But arrive:

```
1
3
2
```

If we blindly apply them:

```
x=10
x=30
x=20
```

we may move the player backwards using stale state.

Applications may therefore use things like:

```
sequence numbers
timestamps
```

to recognize outdated information.

You only need to recognize those mechanisms at this stage.

---

**Duplicate data**

If an operation has side effects, blindly processing duplicates can be dangerous.

Sound familiar?

```
duplicate delivery
↓
duplicate processing
↓
business correctness problem
```

The transport layer and business layer remain separate concerns.

---

**Datagram too large**

UDP datagrams ultimately have to travel through networks with limits on packet sizes.

Large datagrams may require IP fragmentation or fail in problematic ways across paths.

You don't need networking-level MTU calculations here.

Just keep the engineering principle:

> UDP is generally better suited to reasonably sized independent messages than treating it like an unlimited data stream.

---

**No built-in congestion control equivalent to TCP**

TCP contains congestion-control mechanisms designed to adjust sending behaviour when the network becomes congested.

Bare UDP doesn't provide TCP's built-in congestion-control behaviour.

That means a badly designed UDP application could:

```
send
send
send
send
send
send
...
```

without responding appropriately to network congestion.

So fewer guarantees also means:

> More responsibility moves to the application/protocol designer.

## 6. Real Systems

**Voice/video communication**

Imagine:

```
A speaks
↓
audio encoded
↓
small pieces transmitted
↓
B hears audio
```

If one tiny piece disappears, the system may prefer:

```
minor glitch
↓
continue
```

rather than:

```
stop current playback
↓
recover old audio
↓
conversation latency increases
```

Modern real-time systems are considerably more sophisticated than "just use UDP," but the underlying trade-off remains useful:

> Real-time media often prioritizes timeliness over perfect packet recovery.

---

**Online games**

Suppose the server sends:

```
Player position:
10
20
30
40
```

Update 20 disappears.

But update 30 arrives immediately.

The application might simply continue from the newer state.

Recovering 20 could have little value because:

```
30 supersedes 20
```

Again:

> Some information becomes stale extremely quickly.

---

**DNS**

Here's a technology we'll study properly later in Phase B.

Traditional DNS queries often use UDP because many queries are small request/response exchanges:

```
"What IP address belongs to example.com?"

↓ UDP

"93.x.x.x"
```

If the response disappears, the resolver can retry.

However, modern DNS is more nuanced: DNS can also use TCP, and encrypted DNS commonly runs over other transports.

For now:

> DNS is a classic example showing that an application protocol can use UDP while implementing whatever additional behaviour it needs above the transport layer.

---

**HTTP/3 — an interesting modern example**

Traditional HTTP/1.1 and HTTP/2 normally operate over TCP.

HTTP/3 uses **QUIC**, which runs over UDP.

That does not mean HTTP/3 simply gives up reliable delivery.

Instead:

```
UDP
↓
QUIC
↓
reliability, security, stream management, etc.
↓
HTTP/3
```

This beautifully demonstrates our principle:

> UDP provides a minimal transport foundation; higher-level protocols can build stronger guarantees on top of it.

We'll leave QUIC there for now.

## 7. Communication Training

You're designing communication for two systems.

System A — Bank transfer command

```
Transfer £5,000
Alice → Bob
```

Requirements:

```
Every required byte matters.
Ordering matters.
Loss cannot simply be ignored.
```

System B — Real-time multiplayer game

The server sends player-position updates:

```
10:00:00.100 → x=100
10:00:00.120 → x=105
10:00:00.140 → x=110
10:00:00.160 → x=115
```

Suppose the `x=105` update disappears, but `x=110` and `x=115` arrive promptly.

As the engineer, explain:

```
1. Which transport would you initially consider for System A, and why?
I would initially choose TCP because the application requires reliable, ordered delivery of the transfer message. However, TCP only provides transport-level reliability; the banking system still needs application-level mechanisms to protect the financial business invariants.

2. Why might UDP-style communication make sense for System B?
For online games, low latency is more important than perfect delivery.

3. Why could retransmitting the old x=105 update actually be undesirable?
The newer state supersedes the missing update, so retransmitting stale state may add latency without providing useful information.

4. What guarantees are we giving up by choosing UDP?
UDP avoids TCP's built-in retransmission, ordered-delivery and connection-management mechanisms, which can make it suitable for latency-sensitive communication where the application prefers current data over recovery of stale data.

5. Does UDP mean the application cannot implement reliability where it needs it?
Choosing UDP moves responsibility for any required reliability, ordering or recovery to the application or a higher-level protocol.

6. Complete this engineer statement in your own words:
Choosing between TCP and UDP is about matching transport guarantees to application requirements. TCP is appropriate when reliable, ordered delivery is important, while UDP is useful when the application can tolerate loss or wants greater control over reliability and timeliness.
```

## 8. Technology Spotlight — QUIC

You will increasingly encounter the name QUIC in modern web architecture.

At this stage, know only this:

```
HTTP/1.1 → commonly TCP
HTTP/2   → commonly TCP
HTTP/3   → QUIC
QUIC     → runs over UDP
```

QUIC builds features above UDP including:

- reliable delivery
- multiple streams
- congestion control
- encryption integration

Why is this interesting?

Because it proves something important about architecture:

```
UDP itself provides fewer guarantees

BUT

a higher-level protocol
can selectively build the guarantees
it needs on top
```

So don't think:

```
UDP → unreliable application
```

Think:

```
UDP
→ minimal transport guarantees
→ more responsibility/flexibility above it
```

We'll revisit HTTP/3 and QUIC when they become relevant rather than diving into implementation now.

---

# Handbook Page — UDP

## Mental Model

> UDP provides connectionless, datagram-based communication without guaranteeing delivery, ordering or retransmission.

```
Application
    ↓
Datagram
    ↓
UDP
    ↓
Network
    ↓
UDP
    ↓
Application
```

A datagram may:

```
arrive
be lost
arrive late
arrive out of order
```

UDP itself doesn't repair those conditions for the application.

## TCP vs UDP

```
TCP
────────────────────────
connection-oriented
byte stream
reliable delivery
ordered delivery
retransmission

UDP
────────────────────────
connectionless
datagrams
no delivery guarantee
no ordering guarantee
no built-in retransmission
```

But:

> Different guarantees, not simply good protocol vs bad protocol.

## Key Vocabulary

```
UDP
datagram
connectionless
best-effort delivery
packet loss
out-of-order delivery
message boundary
retransmission
timeliness
real-time traffic
sequence number
application-level reliability
```

## Engineering Principles

1. Not every application requires TCP's transport guarantees.

2. Timeliness can matter more than recovering old data.

3. UDP preserves datagram boundaries but doesn't guarantee that datagrams arrive or arrive in order.

4. Giving up transport guarantees moves responsibility to the application or higher-level protocol.

5. UDP does not guarantee low latency; it simply avoids some TCP-style transport coordination.

6. Reliability can be selectively implemented above UDP when required.

## Failure Thinking

When considering UDP, ask:

```
Can data loss be tolerated?

Does ordering matter?

Does old data remain useful?

What happens if a datagram disappears?

Do we need application-level retries?

Could duplicates cause problems?

Do we need sequence numbers?

How will we respond to network congestion?
```

And most importantly:

```
What guarantees does this application
actually require?
```

## Engineer Explanation

```
UDP is a connectionless, datagram-based transport protocol that provides fewer delivery guarantees than TCP. It doesn't guarantee delivery, ordering or retransmission, which reduces transport-level coordination but moves additional responsibility to the application. UDP is particularly useful when timeliness matters more than recovering every lost packet, or when a higher-level protocol wants to implement its own reliability mechanisms.
```
