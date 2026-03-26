# TCP vs UDP

When your browser connects to a server:

```
Browser → TCP/UDP → IP → Internet → Server
```

We already covered:

```
IP → WHERE to send
```

Now:

```
TCP/UDP → HOW to send
```

## TCP — Reliable Communication

TCP is designed for:

- reliability
- correct order
- guaranteed delivery

Example Flow (Simplified)

Before sending data, TCP establishes a connection:

```
// SYN = Synchronize
// ACK = Acknowledge
Client → Server: SYN
Server → Client: SYN-ACK
Client → Server: ACK.
```

After this:

```
connection established
```

Now data can flow.

What TCP Guarantees

- all data arrives
- data arrives in order
- lost packets are retransmitted

**correctness > speed**

### How does TCP know something is lost?

It uses **ACKs (acknowledgements)**.

Example:

```
Packet 1 → received → ACK
Packet 2 → received → ACK
Packet 3 → lost ❌
Packet 4 → received
```

The receiver says:

```
"I expected Packet 3, but didn’t get it"
```

So the sender:

```
re-sends Packet 3
```

This is how TCP maintains:

- no missing data
- correct order

## UDP — Fast but Unreliable

UDP is designed for:

- speed
- low latency
- minimal overhead

How UDP Works

```
send packet → no handshake → done
```

No guarantee:

```
packet may be lost
packet may arrive late
packet may arrive out of order
```

So:

- no automatic resend
- no guarantee of order
- no guarantee of delivery

Real Example, UDP is used for:

- video streaming
- online games
- voice calls

Because: **speed > perfection**

## Real System Insight

When you use: https://api.yourapp.com

Stack looks like:

```
HTTP → TCP → IP
```

When DNS lookup happens:

```
DNS → UDP → IP
```

Different protocols for different needs.
