# 2D DP (Grid DP)

🧩 Problem

Grid:

```
1 3 1
1 5 1
4 2 1
```

Rules:

```
Start at top-left (0,0)
Move only RIGHT or DOWN
Reach bottom-right
Find MINIMUM path sum
```

Define:

```
f(i, j) = minimum path sum to reach cell (i, j) from the top-left
```

To reach (i, j), you can come from:

```
(i-1, j)   ← from above
(i, j-1)   ← from left
```

So how do we compute f(i, j)?

We choose the cheaper path from the two options:

```
f(i, j) = min(f(i-1, j), f(i, j-1)) + grid[i][j]
```

## Big connection (important)

This is similar to BFS shortest path:

```
BFS → explore paths
DP → reuse computed shortest paths
```

## Step by step

**Step 1 — f(0,0)**

```
f(0,0) = 1
```

**Step 2 — first row (can only come from left)**

```
f(0,1) = 1 + 3 = 4
f(0,2) = 4 + 1 = 5
```

**Step 3 — first column (can only come from above)**

```
f(1,0) = 1 + 1 = 2
f(2,0) = 2 + 4 = 6
```

**Step 4 — fill the rest**

```
f(1,1)
min(4, 2) + 5 = 2 + 5 = 7

f(1,2)
min(5, 7) + 1 = 5 + 1 = 6

f(2,1)
min(7, 6) + 2 = 6 + 2 = 8

f(2,2)
min(6, 8) + 1 = 6 + 1 = 7
```

**Final answer**

```
f(2,2) = 7
```

## General rule (VERY IMPORTANT)

```
If problem asks for BEST / MAX → use max()
If problem asks for MINIMUM / CHEAPEST → use min()
If problem asks for NUMBER OF WAYS → use +
```

## DP pattern you should remember

**1️⃣ Climbing stairs**

Goal: count number of ways

So:

```
f(n) = f(n-1) + f(n-2)
```

**2️⃣ House robber**

Goal: maximize money

So:

```
f(i) = max(f(i-1), f(i-2) + nums[i])
```

**3️⃣ Grid minimum path**

Goal: minimize cost

So:

```
f(i, j) = min(f(i-1, j), f(i, j-1)) + grid[i][j]
```

```
State → what am I tracking?
Transition → how do I combine previous states?
Operation → + / max / min depending on goal
```

Same grid movement (right & down):

```
Start → (0,0)
End   → (m-1,n-1)
```

Question: How many unique paths exist?

```
State: f(i, j) = number of unique paths to reach (i, j)
Transition: f(i, j) = f(i-1, j) + f(i, j-1)
Base cases: f(0, 0) = 1, f(0, 1) = 1, f(1, 0) = 1
Operation: +
```

## Grid with obstacles (handling constraints)

**Example:**

```
0 0 0
0 1 0
0 0 0
```

0 = free cell

1 = obstacle (cannot pass)

**Question:** How many unique paths from top-left to bottom-right?

Condition:

if a cell is an obstacle, f(i, j) = 0

```
No path can go through that cell
→ contributes 0 ways
```
