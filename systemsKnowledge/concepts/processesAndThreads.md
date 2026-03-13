# Processes vs Threads

The OS does NOT create a new process for each request.
Creating processes is expensive.

Instead, requests are handled inside the same process.

## What Actually Happens When 10,000 Requests Arrive

Simplified flow:

```
Users → Internet → Server OS → Node Process
```

Inside the Node process::

```
Incoming Requests
       ↓
   Event Queue
       ↓
 Node Event Loop
       ↓
 Your JavaScript Code

```

Requests wait in a queue, and the program processes them.

## Two Main Concurrency Models

Most servers use one of these approaches.

- **Model 1 — Thread per Request**

Many traditional systems do this.

Example servers built with:

- Apache HTTP Server
- Spring Boot
- Django

Architecture:

```
Process
 ├─ Thread 1 → Request 1
 ├─ Thread 2 → Request 2
 ├─ Thread 3 → Request 3
 ├─ Thread 4 → Request 4
```

Each request runs on its own thread.

Advantages:

- simple model
- easy to reason about

Disadvantages:

- threads consume memory
- too many threads → server crash

- **Model 2 — Event Loop (Node.js)**

Node uses a different design.

Example runtime:

- Node.js

Architecture:

```
Single Process
Single Main Thread
        ↓
     Event Loop
        ↓
Event Queue → Request callbacks
```

Instead of creating a thread per request, Node uses:

- non-blocking I/O
- event-driven programming
  This allows _thousands of concurrent requests_.

### Concurrency

Many tasks in progress

Example:

```
Task A waiting for DB
Task B waiting for network
Task C running
```

Node does this.

### Parallelism

Tasks running at the same time on multiple CPUs

Example:

```
CPU 1 → Task A
CPU 2 → Task B
CPU 3 → Task C
```

## Real World Architecture

Example deployment:

```
Server
 ├─ Node Process 1
 ├─ Node Process 2
 ├─ Node Process 3
 └─ Node Process 4
```

Requests are distributed between them using:

- Nginx
- HAProxy

This allows full use of **multiple CPU cores**.

## The Big Systems Insight

When you write backend code, you are actually interacting with:

```
Internet
   ↓
Load Balancer
   ↓
Operating System
   ↓
Processes
   ↓
Threads / Event Loop
   ↓
Your Code
```

## Event Loop Blocking

A CPU-heavy task blocks the event loop.

This is why Node servers should avoid things like:

- large loops
- heavy image processing
- encryption calculations
- complex algorithms
- inside request handlers.

## How Real Systems Solve This

1️⃣ **Worker Threads**
Node can spawn worker threads.

Example concept:

```
Main thread → receives request
        ↓
Send heavy job → worker thread
        ↓
Worker computes result
        ↓
Return result
```

2️⃣ **Job Queue / Background Workers**

Large systems often push heavy work into queues.

Example architecture:

```
User Request
     ↓
API Server
     ↓
Message Queue
     ↓
Worker Service
```

Popular queue systems include:

- RabbitMQ
- Apache Kafka
- Redis (used for queues like BullMQ)

Example:
User requests report.

```
User → API
      ↓
API puts job in queue
      ↓
Worker generates report
      ↓
User downloads later
```

This prevents blocking the server.

## load balancer / reverse proxy

Common tools:

- Nginx
- HAProxy

Architecture:

```
Internet
   ↓
Reverse Proxy
   ↓
Node1   Node2   Node3   Node4
```

## Why Processes Are Preferred Over Threads in Node

Thread → shares memory
Process → isolated memory

Processes are safer because:

> If one process crashes, others keep running
