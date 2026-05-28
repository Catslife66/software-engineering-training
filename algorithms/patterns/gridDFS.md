# Grid DFS

Grid DFS is basically:

```
tree DFS + boundary checks + visited tracking
```

Instead of:

```
node.left / node.right
```

we use directions:

```
up, down, left, right
```

**Key mindset shift**

In trees:

```
structure already prevents cycles
```

In grids/graphs:

```
cycles are everywhere
```

## Drill 1 — Number of Islands

Problem

```
grid = [
  ["1","1","0"],
  ["0","1","0"],
  ["1","0","1"]
]

1 = land
0 = water
```

Return the number of islands.

Connected horizontally/vertically.

Solution:

```
1. What does dfs(r, c) represent?
Explore the entire connected island starting from cell (r, c).

2. Base cases?
if r < 0 or r >= rows or c < 0 or c >= cols:
    return

if grid[r][c] == "0":
    return

if (r, c) in visited:
    return

3. Recursive directions?
visited.add((r, c))

dfs(r + 1, c)
dfs(r - 1, c)
dfs(r, c + 1)
dfs(r, c - 1)

4. When do we increase island count?
When we find an unvisited land cell and start a NEW dfs. Because one DFS completely explores ONE island.

if grid[r][c] == "1" and (r, c) not in visited:
    islands += 1
    dfs(r, c)

5. Why do we need visited?
DFS would revisit the same land forever
```

Code implementation:

```
// DEF explores from one starting point
def dfs(r, c):
    if r < 0 or r >= rows or c < 0 or c >= cols:
        return

    if grid[r][c] == "0":
        return

    if (r, c) in visited:
        return

    visited.add((r, c))

    dfs(r + 1, c)
    dfs(r - 1, c)
    dfs(r, c + 1)
    dfs(r, c - 1)

// the outer loop does the scanning/counting - find starting points

islands = 0
visited = set()

for r in range(rows):
    for c in range(cols):
        if grid[r][c] == "1" and (r, c) not in visited:
            islands += 1
            dfs(r, c)
```
