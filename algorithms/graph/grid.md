# Graph Patterns

## “Grid = Graph”

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

## Clean approach

```
1. loop through every cell (i, j)

2. if cell is land (1) AND not visited:
       count += 1   ← new island found
       run BFS/DFS from (i, j)

3. BFS/DFS will:
       explore all connected land
       mark them as visited

4. continue scanning grid
```

## Drill

You are given a grid:

```
grid = [
    ["1","1","0","0"],
    ["1","0","0","1"],
    ["0","0","1","1"],
    ["0","0","0","0"]
]
```

❓ Question

Return the size (number of cells) of the largest island.

- 1 = land
- 0 = water

Islands are connected horizontally and vertically

Solution:

```
1. Pattern: BFS

2. Why:
We need to explore each connected component of land and measure its size.
Each BFS/DFS gives the size of one island, then we keep the maximum.


Approach:
- Scan every cell in the grid
- If a cell is land and not visited:
    - start BFS from that cell
    - set count = 0 for this island
    - for each land cell visited in BFS, increment count
    - after BFS ends, update max_size
- Return max_size

Walkthrough:
- First island: (0,0), (0,1), (1,0) → size 3
- Second island: (1,3), (2,3), (2,2) → size 3
- Largest island size = 3
```

Code implementation:

```
from collections import deque

def max_island_size(grid):
    if not grid or not grid[0]:
        return 0

    rows = len(grid)
    cols = len(grid[0])
    visited = set()
    max_size = 0

    def bfs(start_row, start_col):
        queue = deque([(start_row, start_col)])
        visited.add((start_row, start_col))
        size = 0

        while queue:
            r, c = queue.popleft()
            size += 1

            directions = [(-1, 0), (1, 0), (0, -1), (0, 1)]

            for dr, dc in directions:
                nr = r + dr
                nc = c + dc

                if 0 <= nr < rows and 0 <= nc < cols:
                    if grid[nr][nc] == "1" and (nr, nc) not in visited:
                        # visited.add(...) happens before queue.append(...)
                        # prevents the same cell from being added to the queue multiple times
                        visited.add((nr, nc))
                        queue.append((nr, nc))

        return size

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == "1" and (r, c) not in visited:
                island_size = bfs(r, c)
                max_size = max(max_size, island_size)

    return max_size
```
