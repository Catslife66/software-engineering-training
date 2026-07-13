# Synchronization

Synchronization is the coordination of concurrent execution to ensure shared resources are accessed safely and consistently.

## 1. Mental Model

Imagine a whiteboard in an office.

Five people are writing on it.

Without any rules:

```
Person A writes
↓
Person B erases
↓
Person C writes
↓
Person D erases
```

Eventually...

Nobody knows what the correct content is.

Now imagine there's one simple rule:

```
Only one person may write at a time.
```

Immediately,

the whiteboard remains correct.

That rule is **synchronization**.

## 2. Why Does Synchronization Exist?

Imagine two worker threads.

Both execute:

```
balance = balance - 100;
```

Current balance:

```
1000
```

Without synchronization:

Thread A reads:

```
1000
```

Thread B also reads:

```
1000
```

Thread A writes:

```
900
```

Thread B writes:

```
900
```

Final balance:

```
900
```

Expected:

```
800
```

This bug is called:

```
Race Condition
```

Notice:

Neither thread was wrong.

The timing was wrong.

Engineer Thinking

Synchronization exists because:

> Concurrency introduces shared state. Shared state introduces concurrent access. Concurrent access introduces race conditions.

Synchronization breaks that chain.

## 3. Engineer Vocabulary

`Shared Resource`

Anything accessed by multiple execution units.

Examples:

```
Variable

Cache

Database Connection

File

Queue

Socket
```

`Shared State`

The data owned by a shared resource.

Example:

```
balance
```

`Critical Section`

A critical section is **the smallest region** of code that accesses shared state and must execute atomically with respect to other execution units.

Example:

```
balance = balance - 100;
```

This looks like one line.

But the CPU actually performs something like:

```
Read balance
↓
Subtract 100
↓
Write balance
```

This is called a **read-modify-write** operation.

If another thread does the same thing at the same time, you get a race condition.

Therefore this whole sequence is the critical section.

`Mutual Exclusion (Mutex)`

A mechanism that ensures only one execution unit can enter a critical section at a time.

Think:

Bathroom.

One key.

One person.

`Lock`

A practical implementation of mutual exclusion - Only one thread may execute this critical section at a time.

Engineers usually say:

```
Acquire the lock.
↓
Execute critical section.
↓
Release the lock.
```

Locks don't protect threads.

They protect shared resources.

Suppose we have:

```
Account A
```

and

```
Account B
```

Should they use the same lock?

No.

They're different resources.

Instead:

```
Account A
↓
Lock A


Account B
↓
Lock B
```

Now:

Someone updating Account A doesn't block someone updating Account B.

Suppose you have:

```
1000 bank accounts
```

Using:

```
1 lock
```

means:

```
Updating one account blocks updates to all accounts.
```

Instead:

```
Account 1
↓
Lock 1

Account 2
↓
Lock 2

...
```

Much more concurrency.

But...

Can we have:

```
1 million locks?
```

Probably not.

Now you've increased:

- memory usage
- complexity
- lock management

Again...

Tradeoffs.

`Atomic Operation`

An operation that is indivisible—it either happens completely or not at all.

Imagine a light switch.

There isn't a halfway position.

It's either:

```
ON
```

or

```
OFF
```

Atomic.

`Lock Contention`

Competition between multiple execution units attempting to acquire the same lock.

The waiting is lock contention

Suppose:

50 threads.

One lock.

Everyone waits.

## 4. Engineer Thinking

Suppose a teammate says:

_"Let's just put locks everywhere."_

Should we?

A senior engineer immediately asks:

```
Will this reduce concurrency?

Will this create contention?

Will throughput decrease?

Can we make the critical section smaller?

Can we avoid shared state altogether?
```

Notice something.

Engineers don't love locks.

They use them when necessary.

### What does "make the critical section smaller" mean?

Imagine this:

```
lock()

read database

calculate discount

call payment API

update balance

unlock()
```

Question:

```
How long is the lock held?
```

Maybe:

```
500 ms
```

During those 500 ms:

Every other thread waits.

That's terrible.

Instead:

```
read database

calculate discount

call payment API

lock()

update balance

unlock()
```

Now the lock is only held for:

```
2 ms
```

Much better.

Senior engineers constantly ask:

```
Can we move work outside the lock?
```

because:

```
Smaller critical section
↓
Less waiting
↓
Less lock contention
↓
Better throughput
```

### Why do engineers try to avoid shared state?

Threads don't introduce shared state.

Threads introduce the **possibility** of shared state.

Imagine:

```
Thread A
↓
Own local variables

Thread B
↓
Own local variables
```

Nothing is shared.

No synchronization needed.

Now imagine:

```
Thread A
↓
Shared Cache

Thread B
↓
Shared Cache
```

Now:

Synchronization.

So the real problem isn't:

```
Threads
```

It's:

```
Mutable Shared State
```

## 5. Tradeoffs

Synchronization gives us:

```
Correctness
↓
Data Consistency
↓
Thread Safety
```

Excellent.

But...

Synchronization also introduces:

```
Waiting
↓
Contention
↓
Reduced Concurrency
↓
Lower Throughput
```

Notice the tradeoff.

Correctness often costs performance.

## 6. Failure Thinking

Let's imagine a poorly designed system.

Pool:

```
100 Worker Threads
```

All need:

```
One Lock
```

What happens?

```
99 Waiting
↓
1 Working
```

The thread pool exists.

But only one thread is actually making progress.

You've accidentally removed most of the benefit of concurrency.

Now imagine something worse.

Thread A:

Needs:

```
Lock 1
```

Then:

```
Lock 2
```

Thread B:

Needs:

```
Lock 2
```

Then:

```
Lock 1
```

Result:

```
A waits for B
↓
B waits for A
↓
Nobody progresses
```

This is:

```
Deadlock
```

We're not diving deeply into deadlocks today.

Just know they're one of the biggest synchronization risks.

## 7. Real Systems

Let's connect this to things you've already learned.

**Thread Pool**

Many worker threads.

Need synchronization.

**Database**

Transactions synchronize concurrent updates.

**Queue**

Multiple consumers may need synchronization internally.

**Redis**

Interesting point.

Remember when I said Redis is mostly single-threaded for command execution?

One reason is:

It avoids many synchronization problems entirely.

That's a tradeoff.

Redis sacrifices some forms of parallel execution to simplify correctness.

## Engineer Explanation

Imagine another engineer asks:

_Why is synchronization important?_

I'd answer:

```
Synchronization coordinates concurrent access to shared resources, ensuring that shared state remains consistent when multiple execution units execute concurrently. Without synchronization, race conditions can occur because multiple threads may read and modify the same data simultaneously.

However, synchronization introduces tradeoffs. Excessive locking increases contention, reduces concurrency, and may even lead to deadlocks if locks are acquired in an inconsistent order. The goal is therefore to synchronize only where necessary while minimizing the size of critical sections.
```

## Vocabulary Notebook

```
Shared Resource

Shared State

Critical Section

Synchronization

Mutual Exclusion

Mutex

Lock

Acquire Lock

Release Lock

Atomic Operation

Thread Safety

Race Condition

Lock Contention

Deadlock

Data Consistency

Critical Section
```

## Engineer's Instinct

When an experienced engineer hears:

_"Many threads access this object."_

Their brain immediately asks:

```
Shared State?

↓

Critical Section?

↓

Need Synchronization?

↓

Could Race Conditions Occur?

↓

Will Lock Contention Become a Bottleneck?

↓

Can We Reduce Shared State Instead?
```

Notice the final question.

This is subtle.

Senior engineers often try to **avoid shared state** rather than simply protecting it.

That's a mindset you'll gradually develop.

## Communication Training

Imagine you're reviewing a pull request from a teammate.

They say:

_"I added a global shared counter that every worker thread updates. I didn't use any synchronization because the code is very small."_

You're reviewing the code.

I want you to respond like an experienced backend engineer.

Don't just say:

"You need a lock."

Explain:

- why the code is dangerous,
- what race condition might occur,
- why the size of the code doesn't matter,
- what tradeoffs synchronization introduces.

```
The code is unsafe because multiple worker threads may update the same shared state concurrently without synchronization. Although the update appears to be a single statement, it is actually composed of multiple operations, such as reading, modifying, and writing the value. Without proper coordination, these operations can interleave, resulting in race conditions and leaving the counter in an inconsistent state.

Synchronization ensures thread-safe access to the shared state, maintaining correctness and data consistency. However, synchronization is not free — it introduces waiting, reduces concurrency, and may lead to lock contention. Therefore, we should synchronize only the critical section while keeping it as small as possible to minimize performance overhead.
```

## Vocabulary Upgrade

**Guarantee Thread Safety**

Instead of:

keep it safe

**Inconsistent State**

Instead of:

wrong value

Engineers say:

leave the data in an inconsistent state

**Interleave**

This is a fantastic concurrency word.

Example:

The operations from different threads may interleave unpredictably.

**Coordination**

Instead of:

working together

**Minimize Shared Mutable State**

Because:

```
Less Shared State
↓
Less Synchronization
↓
Less Lock Contention
↓
Fewer Race Conditions
↓
Simpler Code
```
