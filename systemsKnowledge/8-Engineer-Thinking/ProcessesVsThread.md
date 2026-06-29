# Processes & Threads

## 1. Mental Model

Imagine you're running a restaurant.

The restaurant itself is:

```
A Process
```

Inside the restaurant are workers:

```
Chef
Waiter
Cashier
Dishwasher
```

Each worker is:

```
A Thread
```

The workers:

```
share the same kitchen
share the same food
share the same equipment
```

But they perform different tasks simultaneously.

The restaurant has its own building.

Another restaurant has another building.

The two restaurants cannot simply walk into each other's kitchen.

That is **process isolation**.

**Mental Picture**

```
Restaurant A (Process)

    Kitchen (Shared Memory)

    Chef (Thread)
    Waiter (Thread)
    Cashier (Thread)


Restaurant B (Process)

    Kitchen

    Chef
    Waiter
```

Threads share.

Processes isolate.

That's the entire topic in one picture.

## 2. Why Do Processes Exist?

Imagine every program shared one giant memory.

```
Chrome

VS Code

Spotify

Terminal
```

Suppose Chrome crashes.

Without isolation:

```
Chrome crashes
↓
Memory corrupted
↓
VS Code crashes
↓
Terminal crashes
```

Everything becomes unstable.

Operating systems therefore isolate programs into separate execution environments.

**Engineer Language**

> Each process owns its own virtual address space.

## 3. Why Do Threads Exist?

Now imagine a web server.

A client sends:

```
Request A
```

The server begins processing.

While it's busy:

```
Request B arrives.
```

Without threads:

```
Process
↓
Handle Request A
↓
Only after finishing...
↓
Handle Request B
```

Everyone waits.

Instead:

```
Process
↓
Thread 1 → Request A
Thread 2 → Request B
Thread 3 → Request C
```

Now multiple requests can make progress concurrently.

**Mental Model**

Processes provide:

```
Isolation
```

Threads provide:

```
Concurrency
```

## 4. Engineer Vocabulary

`Process`

An isolated execution environment.

`Thread`

A lightweight execution unit within a process.

`Memory Space`

The memory owned by a process.

Example:

> Every process has its own memory space.

`Shared Memory`

Threads inside the same process share memory.

Example:

> Threads communicate through shared memory.

`Isolation`

Isolation is the separation of components to limit direct interaction and unintended interference.

`Process Isolation`

One process cannot directly access another process's memory.

Example:

> Process isolation improves stability and security.

`Concurrency`

Multiple tasks making progress during the same period.

`Context Switching`

Context switching is the process of saving the execution state of one execution unit and restoring another so the CPU can switch between tasks.

Suppose:

```
CPU
↓
Thread A
↓
Thread B
↓
Thread C
```

The CPU switches between them.

Suppose:

```
Thread A
```

is executing.

CPU stops it.

Before switching,

CPU saves:

```
program counter
registers
execution state
```

Then switches to:

```
Thread B
```

Later:

It restores Thread A.

This entire operation is:

```
Context Switching
```

Context switching isn't free.

CPU spends time:

```
Saving state
↓
Loading state
```

instead of:

```
Executing your code.
```

> Excessive context switching introduces overhead.

`Overhead`

Example:

> Processes have higher overhead than threads.

`Fault Isolation`

Fault isolation asks 'if something fails, can we prevent that failure from spreading?'

Property that prevents failures in one component from propagating to other components.

`Fault Containment`

Fault containment asks 'once a fault has occurred, how much damage is confined to that component?'

Limit the impact of a fault to one component.

Containment is about:

```
limiting damage
```

`Failure Propagation`

A fault spreading through the system.

`Blast Radius`

The scope of damage caused by a failure.

Imagine:

```
Service A crashes.
```

Question:

```
How many other services are affected?
```

That is the:

```
Blast Radius
```

Example:

> We designed the system to reduce the blast radius of individual service failures.

What does that mean?

```
isolate failures
contain failures
prevent propagation
```

## 5. Engineer Thinking

Suppose someone asks:

_Why don't we create one process per request?_

An engineer thinks:

Creating a process means:

- allocating a new memory space
- initializing resources
- loading program state
- scheduling another isolated execution environment

That's expensive.

Creating a thread means:

- reuse existing process
- reuse existing memory
- share resources

Much cheaper.

So backend frameworks often create:

```
One Process
↓
Many Worker Threads
```

rather than:

```
One Process
↓
One Request
↓
Destroy Process
↓
Repeat
```

## 6. Tradeoffs

Every design has tradeoffs.

| Processes              | Threads                        |
| ---------------------- | ------------------------------ |
| Strong isolation       | Shared memory                  |
| Higher overhead        | Lower overhead                 |
| Better fault isolation | Easier communication           |
| Harder communication   | Higher risk of race conditions |

Notice:

Threads are not "better."

They're different.

## 7. Failure Thinking

Now let's ask the engineer question.

Suppose one thread writes:

```
balance = balance - 100;
```

At the same time another thread writes:

```
balance = balance + 50;
```

Both access the same memory.

What could happen?

Potentially:

```
Incorrect balance
```

This is called:

```
Race Condition
```

**Shared memory enables fast communication, but it also introduces synchronization challenges.**

Why Synchronization Exists

Without synchronization:

```
Thread A
↓
writes balance

Thread B
↓
writes balance
```

Result:

```
Race Condition
```

Synchronization prevents that.

**Synchronization** means:

```
One person enters.
↓
Leaves.
↓
Next person enters.
```

In software:

Instead of people:

```
Threads
```

Instead of bathroom:

```
Shared Memory
```

Engineer Definition

> Synchronization is the coordination of concurrent execution to ensure shared resources are accessed safely and consistently.

Notice two important words:

```
coordination
```

and

```
consistently
```

Synchronization isn't about making programs slower.

It's about coordinating access.

## 8. Engineer Explanation

A process is an isolated execution environment with its own memory space, while a thread is a lightweight execution unit within a process that shares the same memory as other threads.

Processes provide strong isolation and fault containment, whereas threads enable efficient concurrency through shared memory and lower resource overhead.

Because threads are less expensive to create and manage, most backend servers use multiple worker threads within a single process to handle concurrent requests. However, shared memory introduces synchronization challenges such as race conditions, so thread safety becomes an important design consideration.

## 9. Real Systems

Let's connect this to systems you've already studied.

**Spring Boot**

One JVM process.

Many worker threads.

**FastAPI**

One or more Python worker processes.

Each worker handles multiple requests (depending on the server configuration).

**PostgreSQL**

Database server process.

Multiple worker processes or threads (implementation varies) handling client connections.

**Browser**

Chrome doesn't use one giant process.

It isolates tabs into separate processes so one crashing tab doesn't necessarily crash the entire browser.

That's process isolation in action.

## 10. Engineer Vocabulary Notebook

```
isolated execution environment

lightweight execution unit

memory space

shared memory

process isolation

efficient concurrency

resource overhead

context switching -> Context switching is the process of saving the execution state of one execution unit and restoring another so the CPU can switch between tasks.

fault isolation

thread safety

race condition

synchronization -> Synchronization is the coordination of concurrent execution to ensure shared resources are accessed safely and consistently.

worker thread

make progress simultaneously

becomes an important design consideration

Operating System Resources

Data Consistency

Shared State

Concurrent Access -> Concurrent access occurs when multiple execution units access the same shared resource during overlapping periods of execution.
```

Reusable Engineering Sentences

```
Threads have lower resource overhead than processes.

Processes are isolated and communicate through inter-process communication (IPC) mechanisms.

Threads communicate through shared memory.

Shared memory introduces synchronization challenges such as race conditions.

Thread synchronization ensures data consistency.

Shared state requires synchronization.

Multiple threads may perform concurrent access to the same object.
```

## 11. Engineer's Observation

An experienced backend engineer immediately notices:

When someone says:

"Multiple threads"

they immediately start thinking:

```
Shared memory
↓
Synchronization
↓
Thread safety
↓
Race conditions
```

When someone says:

"Multiple processes"

they immediately think:

```
Isolation
↓
Higher overhead
↓
IPC
↓
Fault containment
```

When I hear:

"Shared memory"

```
Shared Memory
↓
Shared State
↓
Concurrent Access
↓
Race Conditions
↓
Synchronization
↓
Thread Safety
```

**Why do backend servers usually use multiple threads instead of creating a new process for every request?**

```
Backend servers typically use multiple threads instead of creating a new process for every request because processes have higher resource overhead. Each process owns its own memory space and operating system resources, making process creation relatively expensive.

Threads are much more lightweight because they share the same memory space and resources within a process. This allows backend servers to handle many concurrent requests efficiently while avoiding the overhead of creating a new process for every request.

However, shared memory introduces synchronization challenges. Since multiple threads may access the same data concurrently, thread safety becomes an important design consideration to prevent race conditions and maintain data consistency.
```
