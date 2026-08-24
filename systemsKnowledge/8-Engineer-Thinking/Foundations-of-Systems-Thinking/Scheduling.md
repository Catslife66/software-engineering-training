# Scheduling - What Does the CPU Scheduler Actually Do?

We already understand **processes, threads, concurrency, synchronization and deadlocks**.
Scheduling answers the next question:

> When many threads are capable of running but CPU resources are limited, who actually gets to run?

This topic connects several words you've already encountered---context switching, CPU utilisation, starvation and thread pools - into one mental model.

# 1. Mental Model

Imagine one CPU core and four threads:

```
Thread A ─┐
Thread B ─┤
Thread C ─┤──→ CPU Scheduler ──→ 1 CPU Core
Thread D ─┘
```

All four threads may exist simultaneously.

But one CPU core can execute only **one thread at a particular instant**.

The operating system therefore needs to continually decide:

```
Who can run?
Who should run next?
How long should it run?
When should it be interrupted?
```

That is **scheduling**.

## Thread states

For our purposes, think about three important states.

```
RUNNING
The thread currently has CPU time.

RUNNABLE
The thread could execute,
but is waiting for CPU time.

BLOCKED / WAITING
The thread cannot currently continue.
```

Suppose:

```
A → RUNNING

B → RUNNABLE
C → RUNNABLE

D → WAITING for database
```

The scheduler chooses among runnable threads.

D doesn't need CPU time yet because its progress depends on something else - the database response.

When that response arrives:

```
database response
       ↓
D becomes RUNNABLE
       ↓
scheduler may eventually run D
```

This distinction is fundamental:

> Waiting for CPU and waiting for I/O are different kinds of waiting.

## Time slicing

Suppose A, B and C are all CPU-heavy.

The operating system might conceptually do:

```
CPU timeline →

| A | B | C | A | B | C | A | ...
```

Each gets some CPU time.

That period is often called a **time slice** or **time quantum**.

Because switching happens rapidly, the threads appear to execute concurrently.

But with one core:

> They are **concurrent**, not literally executing in parallel.

With four CPU cores:

```text
Core 1 → Thread A
Core 2 → Thread B
Core 3 → Thread C
Core 4 → Thread D
```

multiple threads can genuinely execute at the same instant.

That's **parallelism**.

So:

```
Concurrency
= multiple tasks make progress over overlapping time

Parallelism
= multiple tasks literally execute simultaneously
```

# 2. Engineer Vocabulary

These are the words I want you to become comfortable using.

### CPU scheduler

The OS component responsible for deciding which runnable thread receives CPU time.

### Runnable

A thread is capable of executing but may currently be waiting for CPU time.

### Running

The thread currently executing on a CPU core.

### Blocked / waiting

The thread cannot continue until some condition or external operation completes.

For example:

```
waiting for:
database
network response
disk
lock
```

### Time slice / time quantum

A period of CPU time allocated to a thread before the scheduler may switch execution.

### Preemption

The operating system interrupts a running thread so another thread can run.

Think:

```text
A is running

scheduler:
"That's enough for now."

A → RUNNABLE
B → RUNNING
```

A doesn't voluntarily have to finish first.

### Context switch

Changing execution from one thread to another.

Conceptually:

```
save A's execution state
↓
load B's execution state
↓
execute B
```

Context switching has **overhead**.

### Fairness

How fairly CPU opportunities are distributed among runnable work.

Fairness doesn't necessarily mean:

```
everyone gets exactly 25%
```

It means the scheduling policy attempts to prevent work from being indefinitely denied execution.

### Starvation

A thread is repeatedly denied the resources or CPU opportunity it needs even though the system as a whole continues making progress.

You've already seen this with locks.

### Priority

Some scheduling systems allow certain work to receive preferential scheduling treatment.

Priority can be useful, but excessive priority differences can contribute to starvation.

### CPU-bound

Work whose progress is primarily limited by CPU computation.

Examples:

```text
image processing
compression
encryption
large calculations
```

### I/O-bound

Work that spends significant time waiting for external input/output.

Examples:

```text
database query
HTTP API call
disk read
network communication
```

This distinction becomes extremely important when reasoning about thread
pools.

# 3. Engineer Explanation

Here's how I want you eventually to be able to explain scheduling:

```
The CPU scheduler determines which runnable threads receive CPU time. When there are more runnable threads than available CPU cores, threads compete for execution time and the scheduler switches between them. Preemption allows the operating system to interrupt running threads so other runnable work can progress, but frequent context switching introduces overhead. Scheduling policies also need to consider fairness, priority and starvation.
```

Now connect that to backend engineering:

```
For I/O-bound workloads, concurrency can improve resource utilisation because while one thread is blocked waiting for external I/O, another runnable thread can use the CPU. For CPU-bound workloads, adding significantly more runnable threads than available CPU cores may provide little benefit and can increase scheduling and context-switching overhead.
```

That second paragraph is particularly valuable.

# 4. Trade-offs

Now we ask our usual engineering question:

> What do we gain, and what does it cost?

## More runnable threads

Suppose:

```text
4 CPU cores
4 CPU-heavy threads
```

Potentially:

```text
Core 1 → A
Core 2 → B
Core 3 → C
Core 4 → D
```

Excellent CPU utilisation.

Now imagine:

```text
4 CPU cores
1,000 CPU-heavy runnable threads
```

We haven't magically created more computing power.

Instead:

```text
1000 runnable threads
        ↓
4 cores
        ↓
constant scheduling
        ↓
more context switching
```

So:

```text
More concurrency
        ↓
can improve utilisation

BUT

too much runnable work
        ↓
scheduling overhead
context switching
memory overhead
resource contention
```

This gives us an important engineer principle:

> **More threads do not mean more CPU capacity.**

## CPU-bound vs I/O-bound

This explains something from our earlier thread-pool lessons.

### CPU-bound workload

```text
Thread
↓
calculate
↓
calculate
↓
calculate
```

The CPU is the scarce resource.

Adding hundreds of threads doesn't create more CPU cores.

### I/O-bound workload

```text
Thread A
↓
database request
↓
WAIT

CPU
↓
runs Thread B
```

Concurrency can be much more useful because threads spend substantial
periods waiting.

So thread-pool sizing depends partly on the workload.

Not:

> More threads = faster.

But:

> **What are the threads spending their time doing?**

That's the engineer question.

# 5. Failure Modes

Scheduling isn't usually something application developers directly control in detail, but its effects appear in production systems.

## CPU saturation

Suppose incoming CPU-heavy work exceeds available processing capacity:

```text
incoming work
>>>>>>>>>>>>>

CPU capacity
>>>>
```

Runnable work accumulates.

Consequences can include:

```text
longer waiting for CPU
↓
higher response latency
↓
lower responsiveness
```

The important diagnosis is:

> **CPU is the bottleneck.**

Adding more worker threads may make this worse rather than solve it.

## Excessive context switching

Imagine:

```text
few CPU cores
+
huge number of runnable threads
```

The CPU spends some of its time switching between work rather than executing useful application work.

Engineer phrase:

> **The application may be oversubscribing the CPU.**

### Thread oversubscription

This means having substantially more active/runnable execution demand than the available processing resources can efficiently serve.

Again:

```text
more threads
≠
more capacity
```

## Starvation

Suppose high-priority work continually receives CPU opportunities:

```text
HIGH priority → run
HIGH priority → run
HIGH priority → run
HIGH priority → run

LOW priority → waiting...
```

The system is progressing.

But low-priority work isn't.

That's **starvation**.

Compare it with our deadlock handbook:

```text
Contention
→ some waiting
→ progress continues

Starvation
→ system progresses
→ particular work may be denied indefinitely

Deadlock
→ dependency cycle
→ participants cannot make progress
```

## Priority inversion

Here is one slightly more advanced term worth recognising.

Imagine:

```text
Low-priority thread
holds Lock X

High-priority thread
needs Lock X
```

The high-priority thread cannot proceed until the lower-priority thread
releases the resource.

So priority alone doesn't determine progress when synchronization
dependencies exist.

You don't need to study the OS solutions yet. Just recognize the term:

> Priority inversion occurs when higher-priority work is indirectly blocked by lower-priority work holding a required resource.

# 6. Real Systems

Let's bring this back to backend engineering.

Imagine a Spring Boot server with a worker thread pool.

Requests arrive:

```text
HTTP Requests
     ↓
Thread Pool
     ↓
Application
```

Suppose each request does:

```text
receive request
↓
small CPU calculation
↓
query database
↓
WAIT 100ms
↓
small CPU calculation
↓
return response
```

Most of the request lifetime may be **I/O waiting**, not CPU execution.

That is why multiple request threads can be useful:

```text
Thread A → waiting DB
Thread B → waiting API
Thread C → RUNNING
Thread D → runnable
```

The CPU can continue doing useful work while other threads wait.

But now imagine every request performs expensive video encoding:

```text
Request
↓
100% CPU for 10 seconds
```

With four cores and hundreds of request threads:

```text
hundreds of CPU-bound threads
            ↓
        four cores
```

Increasing the thread pool doesn't solve the capacity problem.

We would instead investigate things like:

```text
Can we reduce the computation?

Can the work move off the request path?

Should heavy work be queued?

Do we need more compute capacity?

Can we horizontally scale?

Is this workload suitable for asynchronous processing?
```

Notice how scheduling now connects to architecture.

# 7. Communication Training

Now we move from understanding to speaking like an engineer.

## Scenario

Your production service has:

```text
4 CPU cores
200 worker threads
```

During normal traffic:

```text
CPU utilisation: 35%
Response latency: 100ms
```

During peak traffic:

```text
CPU utilisation: 100%
Response latency: 4 seconds
```

A teammate says:

> "Requests are slow. Let's increase the thread pool from 200 to 500."

I want you to respond as the backend engineer.

Address these four points:

1.  What evidence suggests the current bottleneck?
2.  Why might increasing worker threads fail to improve performance?
3.  What scheduling-related cost could additional runnable threads
    introduce?

4.  What would you investigate before changing the thread-pool size?

    We'd look at things such as:

    ```
    CPU utilisation
            +
    number of runnable threads
            +
    thread states
            +
    request profiles
            +
    database latency
            +
    downstream-service latency
    ```

    We want to know:

    ```
    What are those 200 threads actually doing?
    ```

    Are most:

    ```
    RUNNING / RUNNABLE
    → competing for CPU
    ```

    or:

    ```
    BLOCKED
    → waiting for database/network/locks
    ```

```
The 100% CPU utilisation during peak traffic suggests that CPU capacity is currently saturated and may be the bottleneck. Increasing the worker-thread pool from 200 to 500 would not increase the available processing capacity because the system still has only four CPU cores. If the additional workers become runnable, they would compete for the same CPU resources and could increase scheduling and context-switching overhead, potentially worsening response latency.

Before changing the thread-pool size, I would investigate the thread states and workload profile to determine whether requests are primarily CPU-bound or blocked on I/O. For an I/O-bound workload with spare CPU capacity, additional concurrency can improve utilisation because other runnable threads can execute while some workers wait for external I/O. However, downstream capacity such as database connections and external services must also be considered.
```

An engineering habit:

```
Observation:
CPU = 100%
        ↓
Hypothesis:
CPU saturation may be bottleneck
        ↓
Investigation:
thread states + workload + downstream metrics
        ↓
Decision:
then tune architecture/configuration
```

# 8. Technology Spotlight

There isn't a particular infrastructure product you need to learn for
scheduling.

But one Java concept is worth recognising.

### Technology Spotlight: Java ExecutorService

At this stage, all you need to know is:

> `ExecutorService` is a Java abstraction for managing and executing tasks using worker threads.

Instead of manually creating threads constantly:

```java
new Thread(task).start();
```

applications commonly submit work to an executor/thread pool.

Conceptually:

```text
Tasks
  ↓
ExecutorService
  ↓
Worker Thread Pool
  ↓
CPU Scheduler
  ↓
CPU cores
```

Important distinction:

```text
ExecutorService / thread pool
→ application-level management of work and worker threads

CPU scheduler
→ operating-system-level decision about which runnable threads actually execute
```

We'll learn Java concurrency APIs properly when your Java classroom
reaches that stage. Here, recognizing the system relationship is enough.

---

# 📘 Handbook Page --- Scheduling

## Mental Model

> Scheduling is the operating system's mechanism for allocating limited CPU execution time among runnable threads.

A thread may be:

```text
RUNNING
→ currently executing

RUNNABLE
→ capable of executing, waiting for CPU

WAITING/BLOCKED
→ cannot continue until something else happens
```

## Core Relationship

```text
Runnable Threads
       ↓
CPU Scheduler
       ↓
Available CPU Cores
       ↓
Execution
```

When runnable threads exceed available cores:

```text
scheduler shares CPU time
↓
preemption
↓
context switching
↓
threads make concurrent progress
```

## Key Vocabulary

```text
CPU scheduler
runnable
running
blocked / waiting
time slice
preemption
context switch
fairness
priority
starvation
CPU-bound
I/O-bound
CPU saturation
thread oversubscription
priority inversion
```

## Engineering Principles

**1. More threads do not create more CPU capacity.**

**2. I/O-bound concurrency can improve utilisation because other work
can run while threads wait for I/O.**

**3. CPU-bound workloads are constrained primarily by available
processing capacity.**

**4. Excessive runnable threads can increase scheduling and
context-switching overhead.**

**5. Fairness concerns whether runnable work gets reasonable
opportunities to progress.**

**6. Starvation means the system progresses while particular work is
repeatedly denied the resources it needs.**

## Failure Thinking

When a service slows down, don't immediately ask:

> Should we add more threads?

Ask:

```text
What is the bottleneck?

CPU?
Database?
Network?
Lock contention?
Downstream service?

Are threads RUNNING,
RUNNABLE,
or BLOCKED?

Is the workload CPU-bound
or I/O-bound?
```

## Engineer Explanation

```
The CPU scheduler decides which runnable threads receive CPU time. When runnable demand exceeds available CPU cores, the scheduler shares processing time through scheduling and preemption. This enables concurrent progress, but excessive runnable work can increase context-switching overhead. In backend systems, understanding whether a workload is CPU-bound or I/O-bound is important when reasoning about concurrency and thread-pool sizing.
```
