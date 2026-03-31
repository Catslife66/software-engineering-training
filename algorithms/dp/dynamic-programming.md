# Dynamic Programming

```
Solve a smaller problem once.
Store the answer.
Reuse it later.
```

## Why Do We Need Dynamic Programming?

Fibonacci (recursive)

```
def fib(n):
    if n <= 1:
        return n
    return fib(n-1) + fib(n-2)
```

Let’s expand it:

```
fib(5)
= fib(4) + fib(3)

fib(4)
= fib(3) + fib(2)

fib(3)
= fib(2) + fib(1)
```

Problem

We are recomputing the SAME subproblems again and again

This is called: **overlapping subproblems**

## First DP Technique: Memoization

We store results in a dictionary.

```
def fib(n, memo={}):
    if n in memo:
        return memo[n]

    if n <= 1:
        return n

    memo[n] = fib(n-1, memo) + fib(n-2, memo)
    return memo[n]
```

What changed?

```
Before: recompute everything
Now: compute once, reuse forever
```

Complexity improvement

| Version   | Time Complexity |
| --------- | --------------- |
| Recursive | O(2^n) ❌       |
| Memoized  | O(n) ✅         |

## What makes a problem DP?

Two conditions:

1️⃣ Overlapping subproblems

```
same problem appears multiple times
```

2️⃣ Optimal substructure

```
solution depends on smaller solutions
```

Example:

```
fib(n) = fib(n-1) + fib(n-2)
```

## Important mindset shift

Recursion thinking:

```
break problem into smaller pieces
```

DP thinking:

```
reuse the smaller pieces instead of recomputing
```

## Two forms of DP

Later, you will see two common styles.

1. Top-down DP

This is recursion + memoization.

You start from the big problem and recurse downward.

2. Bottom-up DP

This builds answers from the smallest cases upward, often with a loop.

```
def fib(n):
    if n <= 1:
        return n

    # Step 1 — initialize a list
    # e.g n = 5 -> dp = [0, 0, 0, 0, 0, 0]

    # Step 2 — base cases
    # dp[0] = 0
    # dp[1] = 1

    dp = [0] * (n + 1)
    dp[1] = 1      # now dp = [0, 1, 0, 0, 0, 0]

    # Step 3 - fill the table
    for i in range(2, n + 1):
        dp[i] = dp[i-1] + dp[i-2]

    return dp[n]
```

**Even better (space optimization)**

We don’t actually need the whole array.

```
# identifying recurrence
# building solution step-by-step
# couting DP

def climb_stairs(n):

    if n <= 2:
        return n

    prev2 = 1  # f(1)
    prev1 = 2  # f(2)

    for i in range(3, n + 1):
        curr = prev1 + prev2
        prev2 = prev1
        prev1 = curr

    return prev1
```

## Summary

**1. DP is about repeated subproblems**

If the same smaller problem appears again and again, store its answer.

**2. Define the state clearly**

For climbing stairs:

```
f(n) = number of ways to reach step n
```

**3. Think about the last move**

That is often the easiest way to derive the recurrence.

**4. Base cases matter**

Without correct base cases, the recurrence breaks.

### DP Structure

State = meaning

What does f(n) mean? / What does f(n) represent?

f(n) = number of ways to reach step n

Transition = formula

How does f(n) relate to smaller states? / How do I compute f(n)

e.g f(n) = f(n-1) + f(n-2)

Base case

What are the smallest known answers?

e.g: f(1) = 1, f(2) = 2
