# Multi-Source BFS

Grid:

```
2 1 1
1 1 0
0 1 1
```

Legend:

```
2 = rotten orange
1 = fresh orange
0 = empty cell
```

Rule:

```
Every minute,
every rotten orange
rots its 4 neighboring fresh oranges.
```

Question:

```
How many minutes
until ALL oranges are rotten?
```

It is not asking:

```
Shortest path to one cell.
```

Instead it asks:

```
How long does the whole spreading process take?
```

## Drill 1 - Rotting Oranges

Solution:

```
1. What does a queue entry (r, c, minute) represent?
(r, c, minute) means orange at (r,c) becomes rotten at this minute.

2. Why is this multi-source BFS?
because all initially rotten oranges are starting points at minute 0.

3. What does fresh track?
fresh tracks how many fresh oranges remain.

4. When do we decrement fresh?
when a fresh orange changes from 1 to 2.

5. Why do we mutate grid[nr][nc] = 2 immediately?
so it will not be added to the queue again by another rotten neighbour.

6. What does return -1 mean?
some fresh oranges remain unreachable.
```

Walkthrough:

```
1. Minute 0 rotten oranges?
(0, 0)
2. Minute 1 newly rotten oranges?
(0, 1), (1, 0)
3. Minute 2 newly rotten oranges?
(0, 2), (1, 1)
4. Minute 3 newly rotten oranges?
(2, 1)
5. Minute 4 newly rotten oranges?
(2, 2)
6. Final answer?
4
7. What does (r, c, minute) represent?
orange at (r,c) becomes rotten at this minute
```

Code implementation:

```
from collections import deque

def oranges_rotting(grid):
    rows = len(grid)
    cols = len(grid[0])

    queue = deque()
    fresh = 0

    # Step 1: scan grid
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == 2:
                queue.append((r, c, 0))
            elif grid[r][c] == 1:
                fresh += 1

    minutes = 0

    # Step 2: BFS spread
    while queue:
        r, c, minute = queue.popleft()
        minutes = max(minutes, minute)

        for dr, dc in [(1,0), (-1,0), (0,1), (0,-1)]:
            nr = r + dr
            nc = c + dr

            if nr < 0 or nr >= rows or nc < 0 or nc >= cols:
                continue

            if grid[nr][nc] != 1:
                continue

            grid[nr][nc] = 2
            fresh -= 1

            queue.append((nr, nc, minute+1))

    if fresh > 0:
        return - 1

    return minutes
```

Key idea:

```
queue = rotten oranges waiting to spread, with their minute
fresh = fresh oranges still remaining
grid mutation = visited marking
-1 = impossible to rot all oranges
```

## Drill 2 - Impossible Case

Grid:

```
2 1 0
0 1 0
1 0 1
```

Walkthrough:

```
1. Minute 0 rotten oranges?
(0, 0)
2. Minute 1 newly rotten?
(0, 1)
3. Minute 2 newly rotten?
(1, 1)
4. Are there fresh oranges left?
yes, (2, 0) and (2, 2) are fresh
5. Why can’t they rot?
because they are not connected to any rotten source through fresh oranges.
6. Return value?
return - 1
```

This is why we track fresh: it tells us whether any fresh oranges were unreachable.

## Drill 3 - 01 Matrix

Grid:

```
0 0 0
0 1 0
1 1 1
```

Question:

For every cell, return distance to nearest 0.

Solution:

```
1. What are the starting points?
All 0s

2. Why do we start from all 0s, not from each 1?
Because each 0 is already distance 0 from itself.
Starting from all 0s lets distance waves spread outward.
The first time a 1 is reached, it came from the nearest 0.

3. What does a queue entry (r, c, dist) represent?
(r, c, dist) = this cell is dist steps away from the nearest 0 found so far.

4. When a 1 cell is first reached, why is that distance guaranteed to be its nearest 0 distance?
Because BFS expands in increasing distance from all 0s at the same time.
So the first wave that reaches a 1 must be the closest 0 wave.

5. What should visited represent here?
visited = cells whose nearest-0 distance has already been assigned.
```

Key comparison:

```
Rotting Oranges:
queue entry = orange becomes rotten at this minute

01 Matrix:
queue entry = cell has this distance from nearest 0
```

Walkthrough:

```
1. Distance 0 cells?
(0, 0), (0, 1), (0, 2), (1, 0), (1, 2)

2. Distance 1 cells?
(1, 1), (2, 0), (2, 2)

3. Distance 2 cells?
(2, 1)

4. Final distance grid?
0 0 0
0 1 0
1 2 1
```

Code implementation:

```
from collections import deque

def update_matrix(mat):
    rows = len(mat)
    cols = len(mat[0])

    queue = deque()
    visited = set()
    dist = [[0] * cols for _ in range(rows)]

    # Step 1: add all 0s as sources
    for r in range(rows):
        for c in range(cols):
            if mat[r][c] == 0:
                queue.append((r, c, 0))
                visited.add((r, c))

    # Step 2: BFS
    while queue:
        r, c, d = queue.popleft()

        for dr, dc in [(1,0), (-1,0), (0,1), (0,-1)]:
            nr = r + dr
            nc = c + dc

            if nr < 0 or nr >= rows or nc < 0 or nc >= cols:
                continue

            if (nr, nc) in visited:
                continue

            visited.add((nr, nc))
            dist[nr][nc] = d + 1
            queue.append((nr, nc, d + 1))

    return dist
```
