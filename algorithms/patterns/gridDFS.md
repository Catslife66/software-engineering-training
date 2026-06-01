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

> DFS returns nothing. outer loop counts how many times we start DFS on new unvisited land.

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

## Drill 2 - Largest island size

> DFS returns the size of that island, and outer loop tracks the maximum size.

Problem

```
1 1 0 0
0 1 0 1
1 0 0 1
```

Return the size of the largest island.

Solution:

```
1. What should dfs(r, c) return?
return size contributed by this cell

2. Three base cases in the correct order
if r < 0 or r >= rows or c < 0 or c >= cols:
    return 0

3. Recursive step:
return 1 + dfs(down) + dfs(up) + dfs(right) + dfs(left)

4. dfs code structure
def dfs(r, c):
    if r < 0 or r >= rows or c < 0 or c >= cols:
        return 0

    if grid[r][c] == 0:
        return 0

    if (r, c) in visited:
        return 0

    visited.add((r, c))

    return (
        1
        + dfs(r + 1, c)
        + dfs(r - 1, c)
        + dfs(r, c + 1)
        + dfs(r, c - 1)
    )
```

## Drill 3 - Flood Fill

> Flood Fill changes one connected region, not every matching color in the whole grid.

Problem

You are given an image grid:

```
1 1 1
1 1 0
1 0 1
```

Start cell:

```
sr = 1, sc = 1
```

New color:

```
2
```

Change the connected region with the same original color as the start cell.

Expected output:

```
2 2 2
2 2 0
2 0 1
```

Solution:

```
1. What does dfs(r, c) represent?
exploring connected region from (r,c)
and change its original colour to 2

2. What is originalColor?
originalColor = image[sr][sc]

3. Base cases?
if out of bounds:
    return

if grid[r][c] != originalColor:
    return

4. What do we do to a valid cell?
grid[r][c] = newColor

5. Why do we NOT need a visited set if we mutate the grid?
once color changes
we know it has already been processed
```

Code implementation:

```
originalColour = image[sr][sc]

if originalColour == newColour:
    return image

def dfs(r, c):
    if r < 0 or r >= rows or c < 0 or c >= cols:
        return

    if image[r][c] != originalColour:
        return

    image[r][c] = newColour

    dfs(r + 1, c)
    dfs(r - 1, c)
    dfs(r, c + 1)
    dfs(r, c - 1)

dfs(sr, sc)
return image
```

Key rule:

```
Number of Islands → outer loop scans every cell
Flood Fill → start from one cell only
```
