## Question

You are given:

```
grid = [
  [0, 0, 0],
  [0, 1, 0],
  [0, 0, 0]
]
```

Question:

```

Return the number of unique paths from top-left to bottom-right.

You can only move RIGHT or DOWN.
Cells with 1 are obstacles and cannot be used.
```

Solution:

```
1. Pattern: dynamic programming
2. Why: Because the problem is asking how many unique paths and the movement can only be right or down. For each cell: paths to this cell = paths from top + paths from left

3. Approach:
    State:
    dp[i][j] = number of ways to reach cell (i, j)

    Transition:
    if grid[i][j] == 1:
        dp[i][j] = 0
    else:
        dp[i][j] = dp[i-1][j] + dp[i][j-1]

    Base:
    dp[0][0] = 1 if not obstacle

4. Walkthrough:

DP table

Grid:

0 0 0
0 1 0
0 0 0

DP:

1 1 1
1 0 1
1 1 2

5. Final answer: 2
```
