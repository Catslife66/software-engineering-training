# Concurrency Fundamentals

## Concurrency vs Parallelism

Concurrency means:

> multiple tasks making progress

Example:

```
Task A waiting on network
Task B executing
Task C waiting on database
```

Parallelism means:

> multiple tasks running at the same time

Example:

```
CPU Core 1 → Task A
CPU Core 2 → Task B
```

## How the OS Handles Many Programs

Your CPU switches rapidly between processes:

```
Process A → 5ms
Process B → 5ms
Process C → 5ms
```

This is called **CPU scheduling**.

The switching happens thousands of times per second, creating the illusion that everything runs simultaneously.

## Concurrency in Web Servers

For a backend server, thousands of users may send requests simultaneously.

Example flow:
Users → Internet → Server OS → Process → Application code

Servers handle this using techniques such as:

```
threads
event loops
async I/O
worker pools
```

For example, in Node.js:

```
Incoming requests
      ↓
Event queue
      ↓
Event loop
      ↓
Callbacks executed
```

This allows a single process to handle many concurrent requests efficiently.
