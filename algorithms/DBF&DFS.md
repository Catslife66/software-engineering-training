# Graph Patterns

## Pattern 1: “Grid = Graph”

**"Grid + BFS/DFS = Connected Components"**

This is one of the most important realizations.

Whenever you see: **grid / matrix / maze**

You should think: **This is a graph problem**

### Core Template

```
for each cell:
    if land and not visited:
        BFS/DFS
        mark whole island
        count++
```

```
from collections import deque

def bfs(grid, start):
    rows = len(grid)
    cols = len(grid[0])

    queue = deque([start])
    visited = set([start])

    while queue:
        r, c = queue.popleft()

        for dr, dc in [(-1,0),(1,0),(0,-1),(0,1)]:
            nr, nc = r + dr, c + dc

            if 0 <= nr < rows and 0 <= nc < cols:
                if (nr, nc) not in visited:
                    # add your condition here
                    visited.add((nr, nc))
                    queue.append((nr, nc))
```

Steps:

```
Walk through the map
→ see land?
    → is it new?
        → YES → this is a new island
        → explore it fully (BFS)
        → mark everything visited
```

```
from collections import deque
// BFS
def num_islands_bfs(grid):

    rows = len(grid)
    cols = len(grid[0])

    visited = set()
    count = 0

    directions = [
        (-1, 0),
        (1, 0),
        (0, -1),
        (0, 1)
    ]

    for r in range(rows):
        for c in range(cols):

            if grid[r][c] == 1 and (r, c) not in visited:
                queue = deque([(r, c)])
                visited.add((r,c))
                count += 1

                while queue:
                    rr, cc = queue.popleft();
                    for (dr, dc) in directions:
                        nr, nc = rr + dr, cc + dc
                        if 0 <= nr < rows and 0 <= nc < cols:
                            if grid[nr][nc] == 1 and (nr, nc) not in visited:
                                queue.append((nr, nc))
                                visited.add((nr, nc))

    return count

// DFS
def num_islands_dfs(grid):

    rows = len(grid)
    cols = len(grid[0])

    visited = set()
    count = 0

    def dfs(r, c):

        # stop conditions
        if r < 0 or r >= rows or c < 0 or c >= cols:
            return

        if grid[r][c] == 0 or (r, c) in visited:
            return

        # mark visited
        visited.add((r, c))

        # explore neighbors
        dfs(r+1, c)
        dfs(r-1, c)
        dfs(r, c+1)
        dfs(r, c-1)

    for r in range(rows):
        for c in range(cols):

            if grid[r][c] == 1 and (r, c) not in visited:
                count += 1
                dfs(r, c)

    return count
```

## The reusable mental template

For island-style problems

**Count islands**

```
Every new BFS/DFS = one island
```

**Measure island size**

```
Count how many cells one BFS/DFS visits
```

**Largest island**

```
Compute each island size, keep the maximum
```

## Pattern sheet

Grid traversal pattern

Ask these in order:

```
1. What is a node?
2. What counts as a neighbor?
3. When do I start BFS/DFS?
4. What does visited prevent?
5. What am I tracking during traversal?
```

For islands:

```
1. cell
2. up/down/left/right land cells
3. when land and not visited
4. revisiting / recounting / loops
5. count, size, or max size
```
