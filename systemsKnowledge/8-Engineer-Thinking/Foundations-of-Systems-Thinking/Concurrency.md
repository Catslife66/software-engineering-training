# Concurrency

## 1. Mental Model

Imagine a hospital.

There are:

```
10 doctors
100 patients
```

Can every patient be treated at exactly the same instant?

No.

Instead:

```
Doctor A
↓
Patient 1
↓
Patient 2
↓
Patient 3
```

Meanwhile:

```
Doctor B
↓
Patient 4
↓
Patient 5
```

Many patients are making progress.

Nobody expects all 100 patients to be treated simultaneously.

This is concurrency.

Mental Picture

Think:

```
Concurrency
=
Managing many ongoing tasks.
```

Not:

Doing everything at exactly the same time.

That distinction is incredibly important.

## 2. Why Does Concurrency Exist?

Why don't we simply process requests one at a time?

Imagine your shopping website.

```
Customer A
↓
Checkout
```

While checkout is running:

```
Customer B
↓
Must wait.
```

Then:

```
Customer C
↓
Must wait.
```

Your website becomes unusable.

Instead:

```
Thread 1
↓
Customer A

Thread 2
↓
Customer B

Thread 3
↓
Customer C
```

Now multiple requests make progress together.

**Engineer Thinking**

Concurrency exists because:

```
Computers spend a surprising amount of time waiting.
```

Waiting for:

```
disk
database
network
APIs
```

Instead of letting the CPU sit idle,

the operating system schedules another task.

That improves utilization.

## 3. Engineer Vocabulary

`Concurrency`

The ability of a system to make progress on multiple tasks during the same period of time.

`Parallelism`

Executing multiple tasks simultaneously on different CPU cores.

`Scheduling`

The scheduler decides which thread runs next.

Who decides:

```
Thread A
↓
Thread B
↓
Thread C
```

The:

```
Operating System Scheduler
```

The scheduler performs context switches.

`Blocking`

Blocking I/O occurs when an execution unit waits for an external operation to complete before continuing.

Example:

```
readFromDatabase();
```

Thread waits.

This thread is:

```
Blocked
```

> The thread is blocked waiting for I/O.

`Non-blocking`

Instead of waiting:

```
Start database request
↓
Do other work
↓
Return later
```

This is non-blocking.

`I/O`

(Input/Output) is any operation where a program communicates with something outside of its own CPU and memory.

Examples of I/O

Database

```
Application
↓
SELECT * FROM users
```

The application waits for PostgreSQL.

That's database I/O.

File

```
Read photo.jpg
```

The operating system reads the disk.

Disk I/O.

Network

```
GET /users
```

The application waits for another server.

Network I/O.

Web API

```
Stripe
↓
Payment API
```

Waiting for Stripe is network I/O.

**CPU Work vs I/O Work**

CPU Work

```
sort(list);
calculate();
encrypt();
```

CPU is busy calculating.

I/O Work

```
readDatabase();
callStripe();
readFile();
sendEmail();
```

CPU spends most of its time waiting.

`Thread Pool`

A thread pool is a fixed or dynamically managed collection of reusable worker threads used to execute tasks.

Instead of:

```
Create Thread
↓
Destroy Thread
↓
Create Thread
↓
Destroy Thread
```

We have:

```
Create 50 Threads
↓
Reuse
↓
Reuse
↓
Reuse
↓
Reuse
```

Much more efficient.

Why Use Thread Pools?

Because thread creation has overhead.

Creating thousands of threads every second would be expensive.

Instead:

Reuse them.

## 4. Engineer Thinking

Suppose someone says:

_"Let's add more threads."_

A senior engineer immediately asks:

```
Will more threads improve throughput?

Will context switching increase?

Will shared memory become a bottleneck?

Will lock contention increase?

Will database become the bottleneck instead?
```

## 5. Tradeoffs

Concurrency isn't free.

Benefits:

- Better CPU utilization
- Higher throughput
- Lower response latency
- Handle more concurrent requests

Costs:

- Synchronization
- Race conditions
- Deadlocks
- Context switching overhead
- Debugging complexity

## 6. Failure Thinking

Suppose two threads both execute:

```
balance = balance - 100;
```

at the same time.

Question:

```
Will the result always be correct?
```

No.

Why?

Because both threads perform:

```
Read
↓
Modify
↓
Write
```

If these operations interleave incorrectly:

```
Incorrect balance
```

This is a race condition.

Concurrency didn't create the bug.

Concurrent access to shared state created the bug.

## 7. Engineer Explanation

Why is concurrency important in backend systems?

```
Concurrency allows a backend system to make progress on multiple requests during the same period of time, improving resource utilization and overall throughput. Rather than waiting for one request to complete before processing another, the operating system schedules multiple execution units so work can continue while other tasks are blocked on I/O.

However, concurrency introduces additional complexity because multiple execution units may access shared state concurrently. This requires synchronization mechanisms to maintain thread safety and data consistency while minimizing contention and context switching overhead.
```

## 8. Real Systems

**Spring Boot**

```
Incoming Requests
↓
Worker Thread Pool
↓
Each request handled concurrently
```

**FastAPI**

Same idea.

Multiple requests.

Multiple workers.

Concurrency.

**PostgreSQL**

Many client connections.

Concurrent queries.

Need synchronization internally.

**Redis**

Redis intentionally uses a largely single-threaded command execution model for simplicity and predictable performance, avoiding many synchronization problems while still achieving high throughput through its design.

## 9. Vocabulary Notebook

```
Concurrency

Parallelism

Scheduler

Scheduling

Blocking

Blocking I/O

Non-blocking

Thread Pool

Throughput

Resource Utilization

Shared State

Concurrent Access

Synchronization

Thread Safety

Context Switching

Lock Contention

Race Condition

Data Consistency
```

## 10. Engineer's Observation

Someone says:

_"We need higher concurrency."_

My brain immediately asks:

```
Higher concurrency
↓
More threads?
↓
More context switching?
↓
More shared state?
↓
More synchronization?
↓
Database bottleneck?
↓
Can the downstream services keep up?
```

## One New Engineering Principle

**Measure Before You Tune**

Experienced engineers rarely say:

_Add more threads._

Instead they ask:

```
Where is the bottleneck?
```

Maybe it's:

```
CPU
```

Maybe:

```
Database
```

Maybe:

```
Network
```

Maybe:

```
External API
```

Maybe:

```
Disk
```

The number of threads may have nothing to do with it.

This is why you'll often hear:

> Don't optimize blindly. Measure first.

**Performance optimization is not about making one component faster. It's about identifying and relieving the system's current bottleneck.**

Whenever someone proposes a change—more threads, more cache, more replicas, more servers—I want your first instinct to be:

> "What problem are we solving, and how do we know that's the bottleneck?"

If we were in a real design meeting, I'd ask one follow-up.

_"How would we know whether adding threads is actually helping?"_

An engineer would probably say:

```
I'd monitor CPU utilization, request latency, throughput, thread pool utilization, database response time, and queue backlog before deciding whether increasing the thread pool actually improves performance.
```
