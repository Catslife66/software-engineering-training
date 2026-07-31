# Deadlocks

Deadlocks — 1/8
Two locks
Thread A:
lock Account 1
lock Account 2

Thread B:
lock Account 2
lock Account 1

You will identify why neither thread can continue.

Deadlocks — 2/8
Four deadlock conditions

We will derive:

mutual exclusion,
hold and wait,
no preemption,
circular wait.

No memorization first. We will discover them from examples.

Deadlocks — 3/8
Bank transfer code review
public void transfer(Account from, Account to, int amount) {
synchronized (from) {
synchronized (to) {
from.withdraw(amount);
to.deposit(amount);
}
}
}

You will examine two opposite transfers.

Deadlocks — 4/8
Nested resource locks
synchronized (inventory) {
synchronized (paymentService) {
// work
}
}

Another method obtains them in the reverse order.

You will diagnose the cycle.

Deadlocks — 5/8
Database deadlock scenario
Transaction A updates row 1, then row 2.
Transaction B updates row 2, then row 1.

We will connect application locks with database locks.

Deadlocks — 6/8
Lock ordering design

You will compare:

Lock resources in request order

with:

Always lock resources by ascending ID

The goal is to understand why consistent ordering breaks circular wait.

Deadlocks — 7/8
Avoiding long lock ownership

You will review code that:

acquires a lock,
calls an external API,
waits for a database,
then releases the lock.

We will distinguish deadlock risk from contention risk.

Deadlocks — 8/8
Final diagnosis

You will receive:

three threads,
three resources,
a timing sequence,
and partial logs.

Your task will be to determine:

whether it is a deadlock,
which resources are involved,
where the cycle is,
and what design rule was violated.
