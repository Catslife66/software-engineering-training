# BFS Foundation

```
      A
    /   \
   B     C
  / \   / \
 D  E  F  G
```

Instead of:

```
Go as deep as possible.
```

It asks:

```
Explore everything 1 step away first.
Then everything 2 steps away.
Then everything 3 steps away.
```

BFS explores:

```
A
B C
D E F G
```

Level by level.

**The Key Concept**

DFS:

```
Stack behavior
Go deep first

Goal:
Explore a region

Mindset:
Go deep

Typical questions:
Connected?
Size?
Perimeter?
Boundary-connected?
```

BFS:

```
Queue behavior
Go wide first

Goal:
Explore by distance

Mindset:
Expand outward

Typical questions:
Shortest path?
Nearest?
Minimum steps?
Time to spread?
```

**Why can BFS guarantee that distance is shortest?**

```
BFS explores ALL nodes at distance 1
before ANY node at distance 2.

It explores ALL nodes at distance 2
before ANY node at distance 3.

Therefore,
The first time BFS reaches a node,
it must have reached it using the fewest possible steps.
```

## BFS Rule

```
Process nodes in order of discovery.

Because nodes are discovered level-by-level,
this also means BFS explores nodes in increasing distance order.
```

## What is a neighbour?

A neighbour is any node connected by one edge.

```
Neighbor = reachable in one step.

For Graph:
one edge away

For Tree:
parent or child (depending on traversal)

For Grid:
one move away
```

## What is a Frontier?

The boundary between explored and unexplored.

BFS Wave Picture

Graph:

```
        A
      /   \
     B     C
    / \   / \
   D   E F   G
```

After processing:

```
A
```

Frontier:

```
[B, C]
```

because:

```
A explored
B,C discovered but not explored
```

After processing:

```
A, B, C
```

Frontier:

```
[D, E, F, G]
```

because:

```
distance 0 explored
distance 1 explored
distance 2 waiting
```

The queue is holding the frontier.

In BFS:

```
visited = already explored/discovered

queue = frontier
```

## The Three States of a Node

This is the cleanest BFS model.

Imagine every node is in one of three states.

**State 1 — Undiscovered**

```
Never seen before.
Not in visited.
Not in queue.
```

**State 2 — Frontier**

```
Discovered.
In visited.
Waiting in queue.
```

**State 3 — Processed**

```
Discovered.
Removed from queue.
Neighbors already explored.
```

## BFS Mindset

```
Discover D
→ immediately add to visited
→ immediately add to queue
```

## Drill 1 - Shortest Path in an Unweighted Graph (Graph BFS)

### The Big BFS Shortest-Path Rule

```
Full traversal BFS:
stop when queue is empty

Shortest-path BFS:
stop when goal is reached
```

Graph:

```
A
| \
B  C
|   \
D    E
```

Question:

```
What is the shortest distance from A to E?
```

Solution

```
1. What does (node, dist) represent?
node has been discovered.
The shortest known distance from start to node is dist.

2. What queue represents?
The queue represents the frontier of nodes waiting to be explored.

```

**Walkthrough**

Process: (A,0)

```
1. Which neighbors are discovered?
B, C
2. What gets added to the queue?
[(B,1), (C,1)]
3. What does visited become?
visited = {A, B, C}
4. What is the queue now?
queue = [(B,1), (C,1)]
```

Process: (B, 1)

```
1. Which neighbors of B exist?
A, D

2. Which neighbors are already visited?
A

3. Which new neighbor gets discovered?
D

4. What gets added to the queue?
(D, 2)

5. What does visited become?
visited = {A, B, C, D}

6. What is the queue now?
queue = [(C,1), (D,2)]

```

Process: (C, 1)

```
1. Which neighbors of C exist?
A, E

2. Which are already visited?
A

3. Which new neighbor gets discovered?
E

4. What gets added to the queue?
(E, 2)

5. What does visited become?
visited = {A, B, C, D, E}

6. What is the queue now?
queue = [(D,2), (E,2)]

7. What distance is attached to E and why?
C is 1 step from A
E is 1 step from C
therefore E is 2 steps from A
neighbor_distance = current_distance + 1
```

Process: (D, 2)

```

1. If our goal is to find E, can we stop now?
No, because E has not been processed yet.

2. If not, when can we stop?
When E is removed from the queue

3. What shortest distance will be returned?
2
```

**Code Skeleton**

```
from collections import deque

queue = deque()
visited = set()

queue.append((start, 0))
visited.add(start)

while queue:
    node, distance = queue.popleft()

    if node == goal:
        return distance

    for neighbor in neighbors(node):
        if neighbor not in visited:
            visited.add(neighbor)
            queue.append((neighbor, distance + 1))
```

## Drill 2 - Shortest Path in a Grid (Grid Shortest Path)

Grid:

```
S 0 0
1 1 0
0 0 E
```

Legend:

```
S = start
E = exit
0 = open cell
1 = wall
```

You can move:

```
up
down
left
right
```

Question:

```
What is the shortest distance from S to E?
```

**Solution**

```
1. What does (r, c, dist) represent?
Cell (r,c)
has shortest distance dist from start.

2. Why do we mark visited when adding to queue?
This prevents the same cell from being added multiple times.

3. Why do we return immediately when we reach end?
The first time we reach E, we have found the shortest path to E. So we can stop immediately.
```

**Walkthrough**

Process: ((0, 0), 0)

```
1. What valid neighbour gets discovered?
(0, 1)

2. What gets added to the queue?
((0, 1), 1)

3. What does visited become?
visited = {(0, 0), (0, 1)}

4. What is the queue after processing (0,0)?
queue = [((0, 1), 1)]
```

Process: ((0,1), 1)

```
1. What neighbors of (0,1) exist?
up    -> out of bounds
down  -> (1,1) wall
left  -> (0,0) visited
right -> (0,2) open

2. Which neighbors are invalid (wall/out-of-bounds)?
(1, 1)

3. Which neighbors are already visited?
(0, 0)

4. Which new cell gets discovered?
(0, 2)

5. What gets added to the queue?
((0, 2), 2)

6. What does visited become?
visited = {(0, 0), (0, 1), (0, 2)}

7. What is the queue now?
queue = [((0, 2), 2)]
```

Process: ((0,2), 2)

```
1. Which neighbors of (0,2) exist?
up    -> (-1,2) out of bounds
down  -> (1,2)
left  -> (0,1)
right -> (0,3) out of bounds

2. Which are invalid?
(-1, 2)
(0, 3)

3. Which are already visited?
(0, 1)

4. Which new cell gets discovered?
(1, 2)

5. What gets added to the queue?
((1, 2), 3)

6. What does visited become?
visited = {(0, 0), (0, 1), (0, 2), (1, 2)}

7. What is the queue now?
queue = [((1, 2), 3)]

8. What distance is attached to the new cell, and why?
3, because the current cell distance is 2 so add one step to neighbouring cell.
```

Process: ((1, 2), 3)

```
1. Which neighbors are checked?
up    -> (0, 2) visited
down  -> (2, 2)
left  -> (1, 1) wall
right -> (1, 3) out of bounds

2. Which are invalid?
(1, 1), (1, 3)

3. Which are already visited?
(0, 2)

4. Which new cell gets discovered?
(2, 2)

5. What distance gets attached to it?
4

6. What gets added to the queue?
queue = [((2, 2), 4)]

7. When do we know we have found the shortest path?
when we remove E from the queue

8. What shortest distance should be returned?
4
```

**Code Skeleton**

```
from collections import deque

def shortest_path(grid, start, end):
    rows = len(grid)
    cols = len(grid[0])

    queue = deque()
    visited = set()

    queue.append((start[0], start[1], 0))
    visited.add((start[0], start[1]))

    while queue:
        r, c, dist = queue.popleft()

        if (r, c) == end:
            return dist

        for dr, dc in [(1,0), (-1,0), (0,1), (0,-1)]:
            nr = r + dr
            nc = c + dc

            if nr < 0 or nr >= rows or nc < 0 or nc >= cols:
                continue

            if grid[nr][nc] == 1:
                continue

            if (nr, nc) in visited:
                continue

            visited.add((nr, nc))
            queue.append((nr, nc, dist+1))

    return -1
```
