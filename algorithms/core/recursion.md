# Recursion

Recursion leads directly to:

- tree algorithms
- graph algorithms
- DFS
- divide-and-conquer
- backtracking

It’s one of the most important mental models in computer science.

## Why Recursion Uses a Stack

When a function calls itself, the program must **remember where it was** before the call happened.

So every function call creates something called a **stack frame **(or call frame).

That frame stores things like:

- local variables
- parameters
- the return address (where to go after the function finishes)

These frames are stored in a **call stack**.

### Example: A Simple Recursive Function

Python example:

```
def countdown(n):
    if n == 0:
        return
    print(n)
    countdown(n - 1)
```

Call:

```
countdown(3)
```

Stack:

```
countdown(3)  // bottom
countdown(2)
countdown(1)
countdown(0) // top
```

Unwinding the Stack

The stack pops in reverse order:

```
countdown(0) returns
countdown(1) returns
countdown(2) returns
countdown(3) returns
```

## Important Insight

Recursion is basically:

> Stack + function calls

In fact, every recursive algorithm can be rewritten using an explicit stack.

For example:

```
Recursive DFS → Iterative DFS using a stack.
```

## Why This Matters for Algorithms

### Trees

Almost every tree algorithm is recursive.

### Graph algorithms

Depth-First Search (DFS) uses recursion naturally.

### Divide and conquer

Algorithms like:

- merge sort
- quick sort
- binary search

are all recursive.

## One Very Important Rule in Recursion

Every recursive function must have:

1️⃣ Base case

When the recursion stops.

Example:

```
if n == 0:
    return
```

2️⃣ Recursive case

The function calls itself with a smaller problem.

Example:

```
countdown(n - 1)
```

## Important Recursion Insight

Every recursive algorithm has two phases:

👈 **Phase 1 — Going down the recursion tree**

The stack grows.

👈 **Phase 2 — Unwinding**

Results propagate back up.
