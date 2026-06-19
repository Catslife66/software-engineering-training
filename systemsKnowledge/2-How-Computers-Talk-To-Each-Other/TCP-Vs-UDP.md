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

Engineer Vocabulary:

```
connection-oriented
connectionless
makes sure data arrives -> reliable delivery
keeps the order -> ordered delivery
packet loss
acknowledgement (ACK)
retransmission
data integrity
latency
throughput -> How much data can move through the system.
```

TCP is a connection-oriented protocol that provides reliable and ordered delivery of data. It uses acknowledgements and retransmissions to recover from packet loss and ensure that data arrives in the correct order.

TCP introduces additional overhead because it maintains connection state and performs retransmissions.

UDP is a connectionless protocol that prioritizes low latency and high throughput over reliability. It does not guarantee delivery, ordering, or retransmission of packets.

Choose TCP for applications such as HTTPS because data integrity and reliable delivery are critical. Choose UDP for use cases such as video streaming, online gaming, or voice calls, where occasional packet loss is acceptable and low latency is more important than perfect reliability.

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

What is the difference between TCP and UDP, and when would you choose one over the other?

TCP is a connection-oriented protocol that provides reliable and ordered delivery of data. It uses acknowledgement - ACK and retransmission to recover from packet loss.

UDP is a connectionless protocol that prioritises low latency and throughput over reliability. It does not garantee delivery, ordering and retransmission.

I'd choose TCP for HTTPS communications because it gurantees an ordering delivery and any packet loss will be retransmitted. While I'd chhose UCP for video streaming because loss packet during transmission can be tolerated and speed is prioritised.
