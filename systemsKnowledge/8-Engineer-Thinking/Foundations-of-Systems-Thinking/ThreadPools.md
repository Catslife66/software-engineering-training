# Thread Pools

A thread pool is a pool of reusable worker threads that execute incoming tasks.

## 1. Mental Model

Imagine you're managing a customer support centre.

Every phone call arrives.

Option A:

```
Customer calls
↓
Hire a new employee
↓
Answer call
↓
Fire employee
```

Ridiculous.

Option B:

```
20 employees already working
↓
Customer calls
↓
Available employee answers
↓
Employee waits for next call
```

That's exactly what a thread pool does.

The thread pool doesn't create a new thread for every task.

It **reuses** existing worker threads.

## 2. Why Do Thread Pools Exist?

Let's ask the engineering question.

Why not simply create a thread whenever a request arrives?

Imagine:

```
1000 requests arrive in one second.
```

Without a thread pool:

```
Create Thread #1

Create Thread #2

Create Thread #3

...

Create Thread #1000
```

Every thread creation requires:

- memory allocation
- scheduling
- stack allocation
- operating system bookkeeping

That isn't free.

Now imagine tomorrow.

```
10000 requests
```

Now the operating system spends a huge amount of time simply creating threads.

Not processing requests.

Engineers call this:

```
Thread creation overhead
```

> Thread pools reduce thread creation overhead by reusing a fixed or managed set of worker threads.

## 3. How Does a Thread Pool Work?

Let's follow one request.

```
Client
↓
HTTP Request
↓
Thread Pool
↓
Worker Thread
↓
Database
↓
Response
```

Now imagine:

Every worker thread is busy.

New request arrives.

What happens?

Does the server create another thread?

Usually...

No.

Instead:

```
Incoming Request
↓
Task Queue
↓
Wait
↓
Worker Thread becomes available
↓
Execute
```

Notice something.

The queue is inside the thread pool.

## 4. Engineer Vocabulary

`Task Queue`

Not every incoming task immediately gets a thread.

Sometimes it waits.

`Worker Thread`

A reusable thread owned by the pool.

`Pool Size`

How many worker threads exist.

Example:

```
Pool Size = 100
```

`Thread Creation Overhead`

Cost of creating threads.

`Thread Pool Saturation`

Thread pool saturation occurs when all worker threads are busy and additional tasks must wait or be rejected.

Suppose:

```
Pool Size = 100
100 busy
1 new request
```

No worker available.

The pool is:

```
Saturated
```

`Thread Starvation`

Imagine:

```
Pool Size = 20

20 long-running tasks
↓
Short task arrives
↓
Cannot execute
```

There are free CPUs.

But no free worker threads.

This is:

```
Thread Starvation
```

`Throughput`

Requests completed per unit time.

`Resource Utilization`

How effectively CPU and memory are used.

`Thread Pool Utilization`

How busy are the worker threads?

`Root Cause`

Instead of:

Problem.

Engineers ask:

What is the root cause?

## 5. Engineer Thinking

Suppose your manager says:

_"Let's increase the thread pool from 100 to 1000."_

Should we?

A junior engineer says:

More threads = more requests.

A senior engineer asks:

```
Is CPU already busy?

Is the database the bottleneck?

Will context switching increase?

Will memory usage increase?

Will downstream services become overloaded?

Are most threads blocked on I/O?
```

Notice something.

Nobody answered immediately.

Everyone investigated.

## 6. Tradeoffs

Benefits

```
Reuse worker threads
↓
Lower thread creation overhead
↓
Higher throughput
↓
Better resource utilization
```

Costs

```
Large pool
↓
More memory
↓
More context switching
↓
More synchronization
```

Small pool

↓

Requests wait longer.

Higher latency.

Possible queue backlog.

Everything is a tradeoff.

## 7. Failure Thinking

Suppose:

Pool size:

```
50
```

Traffic:

```
500 requests
```

Database suddenly becomes slow.

Each thread waits:

```
2 seconds
```

What happens?

```
50 worker threads
↓
All blocked
↓
No free workers
↓
Incoming requests wait
↓
Task queue grows
↓
Latency increases
↓
Timeouts
↓
Retries
↓
Even more traffic
```

This is a classic production incident.

## 8. Real Systems

**Spring Boot**

Uses a worker thread pool to process incoming HTTP requests.

**Tomcat**

Has configurable thread pools.

**Java Executors**

Literally provides thread pool implementations.

**NGINX**

Interesting exception.

NGINX relies heavily on event-driven, non-blocking I/O rather than one thread per connection.

## Technology Spotlight

For now, all you need to know:

**Tomcat** is the web server embedded in Spring Boot.

When an HTTP request arrives:

```
Request
↓
Tomcat Thread Pool
↓
Worker Thread
↓
Your Controller
↓
Your Service
↓
Repository
↓
Database
```

Notice: Your Java code is already running inside a worker thread.

## Engineer Explanation

Why do backend servers use thread pools?

```
Backend servers use thread pools to efficiently manage concurrency while avoiding the overhead of creating a new thread for every incoming request. Instead, requests are assigned to reusable worker threads, improving throughput and resource utilization.

However, thread pools must be carefully sized. A pool that is too small can become saturated, causing requests to wait in the task queue and increasing latency. Conversely, a pool that is too large may increase memory usage, context switching overhead, and pressure on downstream services such as databases.
```

## Engineer's Observation

Someone says:

_"The website is slow."_

I immediately think:

```
Worker threads busy?
↓
Database slow?
↓
Blocking I/O?
↓
Thread pool saturated?
↓
Task queue growing?
↓
Context switching?
↓
Downstream bottleneck?
```

Notice what's happened.

I'm no longer thinking:

What is a thread pool?

I'm thinking:

Could the thread pool explain the symptoms?

That's the difference between knowledge and systems thinking.

"The server is slow during peak traffic. Let's just increase the thread pool size from 100 to 500."

```
Before increasing the thread pool size, I'd first identify the system's current bottleneck rather than assuming the thread pool is the problem. I'd examine CPU utilization, thread pool utilization, request latency, database response time, and downstream service performance.

If the worker thread pool is saturated but the database is already the bottleneck, increasing the pool size is unlikely to improve throughput because more threads will simply spend their time blocked on I/O. However, if the system has available CPU capacity and downstream services can handle additional load, a larger thread pool may improve concurrency and resource utilization.

That said, increasing the number of worker threads also increases concurrent access to shared state, synchronization complexity, memory usage, and context switching overhead. The goal is not to maximize the thread count but to size the thread pool appropriately for the workload and the system's bottlenecks.
```
