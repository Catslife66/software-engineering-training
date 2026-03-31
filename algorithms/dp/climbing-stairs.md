# DP (Bottom-up)

> count ways -> use +

## Climbing Stairs

You are climbing stairs.

```
You can take 1 step or 2 steps at a time
You want to reach step n
```

Question:

```
How many different ways can you reach the top?
```

**💭 Think about the last move**

To reach step n, you must come from:

Case 1 — last move = 1 step -> came from (n-1)

Case 2 - last move = 2 steps -> came from (n-2)

```
(n-2)(n-1)(n)
  -    -   n

To get to n
n-1 (take 1 step)
OR
n-2 (take 2 steps)
```

```
Base case:
f(1) = 1 -> [1]
f(2) = 2 -> [1+1], [2]

Build up:
f(n) = f(n-1) + f(n-2)

f(3) = f(2) + f(1) = 3
f(4) = f(3) + f(2) = 5
f(5) = f(4) + f(3) = 8

```

## Golden thinking pattern

```
Step 1: define f(n)
Step 2: think about the LAST move
Step 3: list all valid previous states -> f(n-1), f(n-2)
Step 4: sum them -> f(n) = f(n-1) + f(n-2)
```

## Code implementation

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

**State** = meaning

What does f(n) mean? / What does f(n) represent?

f(n) = number of ways to reach step n

**Transition** = formula

How does f(n) relate to smaller states? / How do I compute f(n)

e.g f(n) = f(n-1) + f(n-2)

**Base case**

What are the smallest known answers?

e.g: f(1) = 1, f(2) = 2
