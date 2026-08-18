# Deadlocks

## Drill 1 - Two locks

Imagine our application has two resources:

```
Resource A = Printer
Resource B = Scanner
```

And two threads.

Thread 1

```
1. Acquire Printer
2. Keep Printer
3. Try to acquire Scanner
4. Use both
5. Release both
```

Thread 2

```
1. Acquire Scanner
2. Keep Scanner
3. Try to acquire Printer
4. Use both
5. Release both
```

Now imagine this exact timing:

```
Thread 1                    Thread 2

acquires Printer            acquires Scanner
      ↓                           ↓
owns Printer                owns Scanner
      ↓                           ↓
requests Scanner            requests Printer
      ↓                           ↓
WAIT                         WAIT
```

Questions:

```
1. What resource does Thread 1 currently own?
thread 1 currently owns the printer

2. What resource is Thread 1 waiting for?
thread 1 is waiting for scanner

3. What resource does Thread 2 currently own?
thread 2 currently owns the scanner

4. What resource is Thread 2 waiting for?
thread 2 is waiting for printer

5. Can either thread continue? Why or why not?
No, because each thread holds a resource while waiting for a resource held by the other thread, creating a circular dependency in which neither thread can make progress.
```

Deadlock isn't merely that threads are slow or waiting; they are stuck indefinitely unless something external intervenes.

The structure is:

```
Thread 1
holds Printer
↓
waits for Scanner
↓
held by Thread 2
↓
which waits for Printer
↓
held by Thread 1
        ↖___________↓
```

So when investigating a suspected deadlock, an engineer often asks:

> What does each thread currently hold, and what is each thread waiting to acquire?

## Drill 2 - Four deadlock conditions

Same scenario:

```
Thread 1:
holds Printer
waits for Scanner

Thread 2:
holds Scanner
waits for Printer
```

Consider these four observations:

A. Only one thread can use the printer at a time. -> mutual exclusion

B. Thread 1 keeps holding the printer while waiting for the scanner. -> hold and wait

C. Thread 2 cannot simply take the printer away from Thread 1. -> no preemption

D. There is a cycle: -> circular wait

```
Thread 1
→ waits for Scanner
→ Thread 2
→ waits for Printer
→ Thread 1
```

For example, establish one ordering rule:

```
Always acquire:
Printer → Scanner
```

Then both threads must follow it.

Thread 1:

```
Acquire Printer
Acquire Scanner
```

Thread 2 must also:

```
Acquire Printer
Acquire Scanner
```

Now imagine Thread 1 owns Printer:

```
Thread 1                    Thread 2

owns Printer                waits for Printer
↓
acquires Scanner            WAIT
↓
does work
↓
releases Scanner
releases Printer
                            ↓
                            acquires Printer
                            ↓
                            acquires Scanner
```

There is waiting, but **no cycle**.

That's an extremely important distinction:

**Waiting does not automatically mean deadlock.**

A thread can wait perfectly safely if the resource will eventually become available.

Deadlock requires a dependency cycle where nobody can make progress.

| Condition            | Meaning                                               |
| -------------------- | ----------------------------------------------------- |
| **Mutual exclusion** | A resource can be exclusively owned.                  |
| **Hold and wait**    | A thread holds one resource while waiting for another |
| **No preemption**    | A resource isn't forcibly taken away from its owner   |
| **Circular wait**    | Threads form a cycle of waiting for one another       |

These are the four Coffman conditions. A deadlock requires all four to exist simultaneously. Break at least one, and that deadlock cannot occur.

Let's combine everything.

```
Thread 1 owns Printer
```

Why can't Thread 2 use it?

**A — Mutual exclusion**

```
Only Thread 1 can own it.
```

Why doesn't Thread 1 release Printer while waiting?

**B — Hold and wait**

```
It keeps Printer while requesting Scanner.
```

Why can't we simply take Printer away from Thread 1?

**C — No preemption**

```
Thread 1 must release it itself.
```

But why can't Thread 1 finish and release it?

**D — Circular wait**

```
Thread 1 needs Scanner
↓
Scanner belongs to Thread 2
↓
Thread 2 needs Printer
↓
Printer belongs to Thread 1
```

Now we're stuck.

To prevent the cycle from existing in the first place:

RULE:

```
Everyone acquires
Printer → Scanner

Never
Scanner → Printer
```

Now circular wait cannot form.

**Consistent lock ordering prevents circular wait.**

That distinction will become important:

```
Deadlock prevention
→ design the system so a condition cannot arise

Deadlock recovery
→ deadlock happened; detect it and break it
```

## Drill 3 - A bank transfer

```
public void transfer(Account from, Account to, int amount) {
    synchronized (from) {
        synchronized (to) {
            from.withdraw(amount);
            to.deposit(amount);
        }
    }
}
```

Two requests arrive concurrently:

```
Thread 1:
transfer(alice, bob, 100)

Thread 2:
transfer(bob, alice, 50)
```

The dangerous execution can be:

```
Thread 1                         Thread 2

locks Alice                     locks Bob
    ↓                               ↓
tries to lock Bob               tries to lock Alice
    ↓                               ↓
Bob held by Thread 2            Alice held by Thread 1
    ↓                               ↓
WAIT                            WAIT
```

Neither thread can make progress.

Thread 1 holds the lock on Alice while waiting for Bob, while Thread 2 holds the lock on Bob while waiting for Alice. This creates a circular wait, so neither thread can acquire its second lock and the system deadlocks.

**Why did the developer use two locks?**

This is worth noticing.

The developer wasn't doing something obviously foolish.

A transfer modifies two shared mutable resources:

```
Alice.balance
Bob.balance
```

They wanted to protect the business invariant:

Money should not be created or lost during concurrent transfers.

So they thought:

```
Protect Alice with a lock
+
Protect Bob with a lock
=
Safe
```

Locally, that reasoning makes sense.

But:

```
Thread safety of individual resources
≠
Safety of the overall locking strategy
```

We solved one problem:

```
Race condition
```

and introduced another:

```
Deadlock
```

There's our architecture theme again:

A correctness mechanism can introduce its own failure modes.

## Drill 4 - Lock Ordering

Now let's modify the design.

The engineer establishes this rule:

> Always acquire account locks in ascending account-ID order.

Suppose:

```
Alice ID = 10
Bob ID   = 20
```

Regardless of transfer direction, the code determines:

```
Account first =
        from.getId() < to.getId() ? from : to;

Account second =
        from.getId() < to.getId() ? to : from;

synchronized (first) {
    synchronized (second) {
        from.withdraw(amount);
        to.deposit(amount);
    }
}
```

Now we again receive:

```
Thread 1:
transfer(alice, bob, 100)

Thread 2:
transfer(bob, alice, 50)
```

the rule is:

```
Always lock lower ID first
```

So:

```
Thread 1: transfer(Alice, Bob)
→ lock Alice
→ then Bob

Thread 2: transfer(Bob, Alice)
→ still lock Alice
→ then Bob

Then:

Thread 1:
holds Alice
→ acquires Bob
→ finishes
→ releases

Thread 2:
waits for Alice
→ holds nothing
```

A consistent global lock acquisition order prevents circular wait by ensuring that all threads acquire shared resources in the same deterministic order.

## Drill 5 - Database Deadlock

Our database has two accounts:

```
accounts

id    owner     balance
10    Alice     £100
20    Bob       £100
```

Two transactions execute concurrently.

Transaction A

```
BEGIN;

UPDATE accounts
SET balance = balance - 20
WHERE id = 10;

UPDATE accounts
SET balance = balance + 20
WHERE id = 20;

COMMIT;
```

Transaction B

At almost the same time:

```
BEGIN;

UPDATE accounts
SET balance = balance - 30
WHERE id = 20;

UPDATE accounts
SET balance = balance + 30
WHERE id = 10;

COMMIT;
```

You can think of an UPDATE here as causing the database to protect the affected row with a lock until the transaction completes.

Now imagine this timing:

```
Transaction A                    Transaction B

UPDATE account 10               UPDATE account 20
↓                               ↓
holds lock on row 10            holds lock on row 20

UPDATE account 20               UPDATE account 10
↓                               ↓
WAIT                            WAIT
```

Transaction A holds a lock on account 10 while waiting for account 20, while Transaction B holds a lock on account 20 while waiting for account 10. This creates a circular wait and therefore a database deadlock.

```
Transaction A
holds row 10
→ waits for row 20

Transaction B
holds row 20
→ waits for row 10
```

Therefore:

```
A → waits for B
B → waits for A

= circular wait
= neither can make progress
= deadlock
```

Prevention strategy:

Always update lower account ID first.

Then both transfers become:

```
Transaction A: 10 → 20
Transaction B: 10 → 20
```

If A gets row 10 first:

```
Transaction A                 Transaction B

lock 10                       wait for 10
↓                             holds nothing
lock 20
↓
complete
↓
COMMIT
↓
release locks
                              ↓
                              lock 10
                              lock 20
                              complete
```

There is **lock contention**, but no deadlock.

**Contention** means transactions compete for a resource and may have to wait.
**Deadlock** means they form a dependency cycle in which none can make progress.

---

**One real-world detail**

Databases don't normally just sit there forever when this happens.

Many database systems can detect the deadlock.

Conceptually, the database discovers:

```
Transaction A → waiting for B
Transaction B → waiting for A

        ↖ cycle ↙
```

It can then choose one transaction as a **deadlock victim**, abort/rollback it, release its locks, and allow the other transaction to continue.

For example:

```
A holds row 10
B holds row 20

        DEADLOCK

Database aborts B
↓
B releases row 20
↓
A acquires row 20
↓
A commits

B can potentially be retried
```

So now we have two different strategies:

```
PREVENTION
Consistent lock ordering
→ prevent circular wait from forming

RECOVERY
Detect deadlock
→ abort one transaction
→ release locks
→ retry if appropriate
```

Both are real engineering techniques.

## Drill 6 - Lock Scope

Consider:

```
synchronized (order) {

    order.setStatus("PROCESSING");

    paymentService.charge(order);

    emailService.sendConfirmation(order);

    order.setStatus("COMPLETE");
}
```

Assume:

```
setStatus()              ~1 ms
paymentService.charge()  ~2 seconds
sendConfirmation()       ~1 second
```

So the lock might be held for roughly:

```
3 seconds
```

Suppose 100 threads need the same order lock.

Questions:

```
1. What resource is being protected by the lock?
The protected shared resource is the order object/state.
The payment and email services are not protected by this lock. They are simply called while the order lock is being held.

That's an important distinction:

Protected resource:
order

Work performed while holding its lock:
├── change order status
├── call payment service
├── call email service
└── change order status

2. Why might the developer have added the lock?
The lock is attempting to protect order-state consistency during concurrent access.

3. While Thread A is waiting two seconds for the payment service, what happens to another thread that needs the same order lock?
If Thread A owns the order lock:

Thread A
holds order lock
↓
waits 2 seconds for payment
↓
waits 1 second for email

Thread B:

needs order lock
↓
BLOCKED
↓
wait...

Even though Thread A isn't actively doing useful CPU work during the network wait, it continues owning the lock.


4. Is this necessarily a deadlock? Why or why not?
No, blocking and lock contention are not necessarily deadlocks.

5. What performance problem might appear even without deadlock?
The system gets slower:

Long lock duration
↓
more threads waiting
↓
higher lock contention
↓
less concurrency
↓
lower throughput
+
higher response latency

6. When engineers say “keep the critical section small,” what do you now think that means?
Minimize the amount of work performed while holding the lock.

Especially avoid slow operations such as:

HTTP/API calls
Database queries
File I/O
Email/network calls
Long calculations

when they don't need to occur under the lock.
```

Engineer explanation

A strong answer in a code review would be:

```
I don't immediately see a deadlock because there is no circular wait. However, the lock is held while performing slow external I/O. This increases lock duration and contention, reduces concurrency and throughput, and increases response latency. I would investigate whether the external calls need to remain inside the critical section.
```

## Drill 7 - Deadlock vs Contention vs Starvation

Scenario A

```
Thread A owns lock
Thread B waits
Thread A finishes
Thread B acquires lock
```

Scenario B

```
Thread A holds Lock 1 → waits for Lock 2
Thread B holds Lock 2 → waits for Lock 1
```

Scenario C

```
Thread A repeatedly gets the lock
Thread B waits

Thread A releases it

Thread C gets it
Thread B waits

Thread C releases it

Thread D gets it
Thread B waits
...

Thread B could theoretically run,
but repeatedly fails to get access.
```

A — Lock contention

```
A owns lock
↓
B waits
↓
A finishes
↓
A releases lock
↓
B acquires lock
```

Nothing is fundamentally broken.

There is competition for a shared resource, so performance can suffer, but progress continues.

**Lock contention occurs when multiple threads compete for the same lock, causing some threads to wait until the lock becomes available.**

B — Deadlock

Thread A waits for a resource held by Thread B, while Thread B waits for a resource held by Thread A.

That's the key diagnostic pattern:

```
A waits for B
↑           ↓
└───────────┘
```

Nobody involved in the cycle can make progress.

**Deadlock occurs when threads form a circular dependency on resources, preventing all threads in that cycle from making progress.**

C — Starvation

The system as a whole is still progressing:

```
Thread A ✓
Thread C ✓
Thread D ✓
Thread E ✓

Thread B ...
          waiting
          waiting
          waiting
```

Other threads keep acquiring the resource while B repeatedly loses out.

That's why we call it **starvation**: the system isn't frozen, but one participant is being denied the resource it needs.

This connects to a term from our future Scheduling section:

**Fairness**

A fair locking/scheduling policy tries to ensure waiting threads eventually receive access rather than allowing one to wait indefinitely.

---

**The mental model**

Don't identify these problems simply from:

"A thread is waiting."

Ask:

**Who is making progress?**

Then:

```
CONTENTION

Some thread owns resource
↓
Owner progresses
↓
Releases resource
↓
Waiting threads can progress
```

versus:

```
DEADLOCK

A waits for B
B waits for A
↓
Nobody in cycle progresses
```

versus:

```
STARVATION

System continues progressing
↓
Other threads keep succeeding
↓
One particular thread repeatedly cannot obtain resource
```

Contention reduces performance because threads compete for shared resources, but progress continues.
Deadlock prevents all participants in a dependency cycle from making progress.
Starvation occurs when the system continues making progress, but a particular thread is repeatedly denied the resources it needs.

## Drill 8 - Final diagnosis

There are three threads and three resources:

```
Resource A = Customer lock
Resource B = Order lock
Resource C = Inventory lock
```

The logs show:

```
Thread 1
holds Customer
waiting for Order

Thread 2
holds Order
waiting for Inventory

Thread 3
holds Inventory
waiting for Customer
```

Meanwhile, requests involving these resources stop completing.

You're the engineer investigating the incident.

Questions:

1. Is this contention, starvation, or deadlock?
   This is deadlock.

```
Thread 1 → waits for Thread 2
Thread 2 → waits for Thread 3
Thread 3 → waits for Thread 1
                    ↓
              dependency cycle
```

Nobody in that cycle can make progress.

2. Draw the dependency chain starting from Thread 1.

```
Thread 1
holds Customer
waits for Order
        ↓
Thread 2
holds Order
waits for Inventory
        ↓
Thread 3
holds Inventory
waits for Customer
        ↓
Thread 1
```

3. What is the specific structural evidence that proves your diagnosis?

The wait-for dependency graph contains a cycle.

```
T1 → T2 → T3
↑           ↓
└───────────┘
```

A cycle in the wait-for graph is the structural evidence we're looking for.

4. Which of the four deadlock conditions can you identify?
   Mutual exclusion

Each resource is exclusively held:

```
Customer  → Thread 1
Order     → Thread 2
Inventory → Thread 3
```

Hold and wait

Every thread holds something while requesting something else:

```
T1: holds Customer  → waits for Order
T2: holds Order     → waits for Inventory
T3: holds Inventory → waits for Customer
```

No preemption

```
The waiting threads cannot simply steal locks from their owners.
```

Circular wait

```
Customer
→ Order
→ Inventory
→ Customer
```

All four conditions exist simultaneously.

5. How could a global lock-ordering rule prevent this?

We could equally establish:

```
Customer
↓
Order
↓
Inventory
```

The important property is:

Every code path must follow the same global ordering.

Suppose we choose:

```
Customer → Order → Inventory
```

A thread needing only Order + Inventory:

```
Order → Inventory
```

A thread needing Customer + Inventory:

```
Customer → Inventory
```

A thread needing all three:

```
Customer → Order → Inventory
```

What must never happen is:

```
Inventory → Customer   ❌
```

because that reverses our global ordering.

We're not eliminating contention.

We're eliminating circular wait.

```
Consistent lock ordering
        ↓
circular wait prevented
        ↓
deadlock prevented

BUT

threads may still compete
        ↓
contention can remain
```

6. Your teammate says "Let's just add more worker threads." Would that solve the problem? Why or why not?

```
Increasing the worker-thread count would not resolve the incident because the root cause is a deadlock, not insufficient processing capacity. The blocked threads form a circular wait, so adding more threads does not release the resources involved. The correct response is to break or prevent the dependency cycle, for example through consistent lock ordering or deadlock recovery.
```
