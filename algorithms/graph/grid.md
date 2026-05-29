## Problem

You are given a grid:

grid = [
["1","1","0","0"],
["1","0","0","1"],
["0","0","1","1"],
["0","0","0","0"]
]
❓ Question
Return the size (number of cells) of the largest island.
1 = land
0 = water
Islands are connected horizontally and vertically

## Solution

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

## Code implementation

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

## Mental template

```
scan grid
→ if land and not visited:
    start BFS
    count island size
    update max
```
