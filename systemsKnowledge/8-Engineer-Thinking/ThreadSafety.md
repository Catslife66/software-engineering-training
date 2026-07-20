# Thread Safety

> Thread safety means correctness under concurrent execution.

Synchronization is only one way to achieve it.

## 1. Mental Model

Imagine a vending machine.

100 people use it.

One after another.

The machine always works correctly.

Now imagine:

100 people all press buttons at the same time.

Question:

Does the machine still produce the correct result every time?

If yes...

The machine is **thread-safe**.

## 2. Why Do We Need Thread Safety?

Let's revisit our counter.

```
counter++;
```

Looks harmless.

But the CPU performs:

```
Read
↓
Add 1
↓
Write
```

Two threads may interleave these operations.

Result:

Wrong answer.

The code is:

```
Not Thread Safe
```

Now imagine:

```
counter++;
```

protected by synchronization.

Every update is correct.

Now:

```
Thread Safe
```

Notice something.

The code changed very little.

The correctness changed completely.

## 3. Engineer Vocabulary

`Thread Safety`

A component is thread-safe if it behaves correctly when accessed concurrently by multiple threads.

Notice:

Behaves correctly.

Not:

Runs faster.

`Race Condition`

A race condition is one reason code is not thread-safe.

`Data Consistency`

Example:

Thread safety maintains data consistency under concurrent access.

`Atomicity`

An operation is atomic if other execution units cannot observe it in a partially completed state.

`Immutable Object`

Example:

```
String
```

Once created:

Cannot change.

Immutable.

`Stateless`

A stateless component keeps no shared mutable data between requests.

This often makes thread safety much easier.

## 4. Engineer Thinking

Suppose your teammate says:

_"Our class isn't thread-safe."_

Question.

What does an experienced engineer immediately ask?

```
Where is the shared mutable state?

↓

Can we remove it?

↓

Can we make it immutable?

↓

Can we make it local?

↓

Do we actually need synchronization?
```

Synchronization isn't the first thought.

Reducing shared mutable state is.

## 5. Tradeoffs

Let's compare several approaches.

**Locks**

Benefits:

- Correctness.

Costs:

- Waiting.

- Contention.

**Immutable Objects**

Benefits:

- Naturally thread-safe.

- Almost no synchronization.

Costs:

- More object creation.

- Sometimes higher memory usage.

**Local Variables**

Benefits:

- No sharing.

- No synchronization.

- Very simple.

Costs:

- Sometimes impossible.

## 6. Failure Thinking

Imagine:

```
List<Order> orders;
```

Multiple worker threads:

```
orders.add(...)
```

No synchronization.

Question:

Will it always work?

Maybe.

Maybe not.

Sometimes.

Those are the worst bugs.

They appear:

```
Today
↓
Disappear
↓
Tomorrow
↓
Reappear
↓
Production only
```

Engineers call these:

```
Concurrency Bugs
```

They're notoriously difficult to reproduce because they depend on timing.

## 7. Real Systems

Let's connect everything you've already learned.

**Spring Boot**

Controllers are typically shared objects.

Many worker threads may call them concurrently.

Therefore:

Avoid mutable shared fields in controllers.

Instead:

Use method-local variables or properly synchronized shared state.

**FastAPI**

Similar principle.

Requests should not accidentally share mutable state.

**HTTP**

Remember:

Stateless.

This is one reason stateless services scale well and are easier to make thread-safe.

**Redis**

Redis simplifies thread safety for command execution by processing commands sequentially.

That's another architectural tradeoff.

## Technology Spotlight

**Spring Beans**

You'll eventually hear:

```
Singleton Bean
```

Many beginners think:

One object.

Safe.

Actually:

One object.

Many worker threads.

Therefore:

That singleton must be thread-safe.

This catches many new Java developers.

We'll revisit it during Spring Boot.

## Engineer Explanation

Imagine another engineer asks:

_What does thread-safe actually mean?_

```
Thread safety means a component behaves correctly even when accessed concurrently by multiple threads. The goal is to maintain correctness and data consistency regardless of the execution order of individual threads.

Thread safety can be achieved in several ways. Synchronization protects shared mutable state from concurrent modification, while immutability and local variables avoid shared state altogether. Engineers generally prefer reducing shared mutable state because it simplifies the design and minimizes synchronization overhead.
```

## Vocabulary Notebook

```
Thread Safe

Not Thread Safe

Data Consistency

Atomicity

Immutable Object

Mutable Object

Local State

Shared Mutable State

Concurrency Bug

Execution Order

Stateless Component
```

## Engineer's Instinct

Here's how my brain works now.

Suppose I review this code:

```
public class CounterService {

    private int counter = 0;
}
```

Before reading another line, my brain immediately asks:

```
Singleton?
↓
Shared?
↓
Mutable?
↓
Accessed by multiple threads?
↓
Thread-safe?
```

Notice how quickly those questions come.

That's the instinct we're building.

## Communication Training

Imagine you're reviewing a Spring Boot service.

Your teammate writes:

```
@Service
public class OrderService {

    private int processedOrders = 0;

    public void processOrder(...) {
        processedOrders++;
        ...
    }
}
```

They say:

_"It's only an integer, so we don't need to worry about thread safety."_

I'd like you to review this code like an experienced backend engineer.

I don't want code changes yet.

I want you to explain:

```
Why this might be unsafe.
Where the shared mutable state is.
Why multiple worker threads matter.
What engineering concerns this introduces.
What general approaches (not specific Java APIs yet) could make the design thread-safe.
```

1. `private` does not make the field thread-safe

private means:

Code outside OrderService cannot directly access this field.

It is an encapsulation rule.

It does not mean:

Only one thread can access this field.

All threads executing methods on the same OrderService object may still reach the field indirectly:

```
public void processOrder(...) {
    processedOrders++;
}
```

So:

```
private = restricts access between classes

thread-safe = maintains correctness during concurrent access
```

2. Why Spring Boot matters here

In Spring Boot, a service annotated with `@Service` is normally created as a singleton bean.

For your current mental model, this means:

```
One OrderService object
        |
        | accessed by
        v
Many request-handling threads
```

Therefore:

```
@Service
public class OrderService {
    private int processedOrders = 0;
}
```

contains:

```
One shared field
+
mutable value
+
multiple worker threads
=
shared mutable state
```

That is why an experienced Java engineer becomes cautious when seeing mutable fields inside a Spring service.

3. Is every field in a shared service dangerous?

No.

Consider:

```
@Service
public class OrderService {

    private final OrderRepository orderRepository;
}
```

The repository reference is assigned during construction and is not repeatedly changed by request methods.

That is very different from:

```
private int processedOrders = 0;
```

which changes every time an order is processed.

Also, method-local variables are normally separate for each method invocation:

```
public void processOrder(Order order) {
    int itemCount = order.getItems().size();
}
```

Each thread gets its own itemCount for its own method call.

So we can distinguish:

```
Mutable instance field
→ potentially shared between request threads

Method-local variable
→ normally belongs to one method invocation
```

Answer:

```
processedOrders raises two separate concerns. First, it is in-memory state tied to the lifecycle of one OrderService instance, so it will be lost when the application restarts and will not represent a system-wide count when multiple application instances are running.

Second, making the field private provides encapsulation, but it does not guarantee thread safety. A Spring service is normally shared by multiple request-handling threads. Those threads may concurrently execute processOrder() on the same service object and update the same mutable field.

Because processedOrders++ is a read-modify-write operation rather than one guaranteed atomic operation, thread execution may interleave and cause lost updates. We should therefore decide whether the counter is needed at all, whether it can be stored outside the shared service, or whether concurrent updates require an appropriate thread-safe coordination mechanism.
```
