# Messaging System (like WhatsApp)

A messaging system has 4 main jobs:

```
1. Store messages safely
2. Deliver messages in real time if receiver is online
3. Deliver missed messages later if receiver is offline
4. Avoid duplicates / track status
```

## Requirements

**Functional requirements**

What features should the system support?

(Example: 1-to-1 messaging, group chat, etc.)

```
1. User can send and receive 1-to-1 messages
2. User can participate in group chats
3. User can view message history
4. User can delete messages
5. Optional: friend/contact management
```

**Non-functional requirements**

What matters most?

(Example: latency, reliability, scale, etc.)

```
1. Low latency message delivery
2. High availability
3. Scalable for large traffic
4. Durable message storage
5. Secure communication
```

## Key Components

**1. WebSocket Gateway**

Keeps users connected in real time.

```
userId → connectionId/serverId
```

It helps answer:

```
Is this user online?
Which server are they connected to?
```

**2. Presence Store**

Now system grows:

```
Load balancer
   ↓
Server 1   Server 2   Server 3

❗ New Problem
A connected to Server 1
B connected to Server 3
```

👉 How does Server 1 find B?

Solution: Presence store = who is online and where

Usually Redis.

Stores temporary online status:

```
user_123 → websocket_server_4
```

This is not permanent data. It just helps route messages.

**3. Database**

Stores durable message history.

```
message_id
chat_id
sender_id
content
created_at
status
```

This is the source of truth.

**4. Pub/Sub or Message Broker**

Used to deliver messages between WebSocket servers -> communication between servers

Now:

```
A → Server A
B → Server B

❗ Problem

Server A cannot directly push to B.
```

Solution: Pub/Sub (Message Broker)

```
Server A receives message
↓
publishes message
↓
Server B receives message
↓
pushes to receiver
```

**5. Notification Service**

Used when the receiver is offline.

```
receiver offline → send push notification
```

## Full Flow

Suppose User A sends “hello” to User B.

```
1. User A sends message through WebSocket to WS Server 1
2. WebSocket server 1 receives it
3. Server authenticates User A
4. Message service checks User A belongs to the chat
5. Message is saved in DB
6. Server sends ACK to User A: "message saved"
7. Server checks if User B is online with Redis presence store: WS Server 2
8. If online:
   WS server 1 publish message event through broker
   WS server 2 receives the event
   WS server 2 pushes the message to User B through User B's WebSocket connection
   User B's client can send delivery/read ACKs later
9. If offline:
   message stays in DB as unread/undelivered
   notification service sends push notification
```

When User B Is ONLINE

1. Save message to DB
2. Find receiver in Redis
3. Publish via Pub/Sub
4. Push via WebSocket

```
User A → WS Server 1
↓
DB stores message
↓
Redis presence says User B is on WS Server 3
↓
Pub/Sub sends event to WS Server 3
↓
WS Server 3 pushes message to User B
```

When User B Is OFFLINE

1. Save message to DB
2. No realtime delivery
3. Receiver reconnects
4. Fetch from DB

```
User A sends message
↓
DB stores message
↓
Presence store says User B offline
↓
No realtime push
↓
Push notification sent
↓
User B opens app later
↓
App fetches missed messages from DB
```

## Core API / actions

```
// send message
POST /chats/{chatId}/messages

// fetch chat history
GET /chats/{chatId}/messages?before={timestamp}&limit=50

// create group
POST /groups

// send group chat message
POST /groups/{groupId}/members

// delete chat
DELETE /messages/{messageId}
```

## High-level architecture

```
Client
↓
WebSocket Gateway
↓
Message Service
↓
Database

Realtime delivery:
Message Service → Pub/Sub Broker → WebSocket Server → Receiver

Offline delivery:
Receiver reconnects → fetch missed messages from DB
```

## Why DB and Pub/Sub Both Exist

This is the key idea:

```
Database = durability
Pub/Sub = realtime delivery
```

The database makes sure messages are not lost.

Pub/Sub makes delivery fast.

Do not rely only on Pub/Sub, because if the receiver is offline, the message disappears unless it is stored.

Do not rely only on DB for realtime delivery, because constantly polling DB is slow and expensive.

## Realtime Messaging Problem

Think about this: You send a message to a friend.

Question:

How does your friend receive it instantly?

Option 1 — Polling (no WebSocket)

Your friend’s app could do:

```
every 1 second → ask server "any new messages?"
```

Example:

```
GET /messages?since=last_timestamp
```

Problem with this approach

```
❌ wasteful (many empty requests)
❌ high server load
❌ not truly real-time (delay depends on polling interval)
```

**Option 2 - WebSocket Gateway**

```
A long-lived connection between client and server

userId → connectionId/serverId
```

Unlike HTTP:

```
HTTP → request → response → closed
WebSocket → open → stay open → send anytime
```

**How messaging works with WebSocket**

```
1. User A sends message → server
2. Server stores message
3. Server pushes message instantly → User B (via open connection)
```

👉 No need to “ask again”

**Visual Flow**

```
User A → Server → User B
         (push instantly)
```

**Why WebSocket is used**

```
✔ real-time delivery
✔ no polling overhead
✔ lower latency
✔ better user experience
```

```
HTTP = knocking on the door each time -> ask repeatedly
WebSocket = staying on a phone call -> stay connected
```

## Message Ordering Problem

Imagine User A sends three messages quickly:

```
1. Hi
2. Are you free?
3. Let's meet at 5
```

The receiver should see them in that order.

But distributed systems can mess this up because:

```
network delay
retry
different servers
async delivery
```

So B might receive:

```
2. Are you free?
1. Hi
3. Let's meet at 5
```

That’s bad user experience.

**Solution: server assigns order**

Do not rely on client time.

Bad:

```
client sentAt decides order
```

Why?

```
client clocks can be wrong
network delays vary
messages can arrive out of order
```

Better:

```
server assigns message sequence/order
```

Example:

```
chat_id: chat_123
message_seq: 101
message_seq: 102
message_seq: 103
```

Then clients display messages by:

```
chat_id + message_seq
```

## Duplicate Message Problem

Sometimes this happens:

```
Client sends message
Server saves it
ACK gets lost
Client retries
```

Without protection, DB stores it twice.

**Solution: idempotency key**

Client creates a unique ID for each message:

```
clientMessageId
```

Client sends:

```
{
  "clientMessageId": "abc-123",
  "chatId": "chat-1",
  "content": "hello"
}
```

DB enforces:

```
unique(sender_id, client_message_id)
```

So retry is safe:

```
first send → save message
retry same ID → return existing message, no duplicate
```

## Acknowledgement flow

Messaging systems usually use ACKs.

```
Client sends message
↓
Server stores message
↓
Server sends ACK: saved
```

ACK promises that data is safe: Store → then ACK → then deliver

Flow:

```
server saves message → sent
receiver gets message → delivered
receiver opens chat → read
```

## Backpressure & Scaling

What if millions of users send messages at the same time?

👉 System overload:

```
servers overwhelmed
queue grows
latency increases
failures happen
```

😰 Incoming rate > processing rate

**Solutions (High-Level)**

```
Rate limiting
Queue buffering
Autoscaling workers
Load shedding (reject requests)
```

## Mental Model

```
1. Store message (DB)
2. ACK to sender
3. Deliver via WebSocket (if online)
4. Otherwise(offline) store + fetch later
5. Maintain order (sequence)
6. Prevent duplicates (idempotency)
7. Handle scale (queue + backpressure)
```

This system is difficult because it combines:

```
realtime communication
distributed servers
durable storage
offline recovery
duplicate prevention
status tracking
```
