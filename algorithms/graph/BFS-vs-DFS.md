# Graph Algorithms

Trees are actually a special type of graph.

Graphs are used in:

- Google Maps routing
- social networks
- recommendation systems
- network routing
- compilers
- distributed systems

Two core graph algorithms:

- DFS (Depth-First Search)
- BFS (Breadth-First Search)

And they connect directly to:

- recursion
- stacks
- queues
- trees

## DFS vs BFS

### DFS (Depth-First Search)

> "go deep first"

> You are in a maze → pick a path → go as far as possible → dead end → backtrack

Uses a stack. Because DFS **goes as deep as possible first**, and stacks naturally support that behavior.

Rule of stack: Last In → First Out

That means the most recently discovered node is explored first.

Think of DFS like exploring a maze by going deep down one path.

DFS is good for

- exploring all paths
- backtracking problems
- cycle detection
- topological sorting
- tree traversals

_DFS backtracks because it commits to one path_

**DFS Using Recursion**

```
def dfs(node):

    if node is None:
        return

    print(node.value)

    dfs(node.left)
    dfs(node.right)
```

Walkthrough:

```
Problem:
Explore everything reachable from a node.

Mental Picture:
Keep going deeper.
A
|
B
|
C
|
D

State:
visited - Nodes that have already been explored.

Core Operation:
dfs(neighbour)
```

### BFS (Breadth-First Search)

> "expand evenly"

> You are flooding the maze with water → it spreads everywhere evenly → first time it reaches goal = shortest path

Uses a queue. Because BFS explores nodes **level by level**.

Rule of queue: First In → First Out

Nodes are processed in the order they were discovered.

Think of BFS like expanding circles outward.

Key idea:

- enqueue children
- dequeue next node

BFS is good for

- shortest path in unweighted graphs
- minimum steps problems
- level order traversal
- spreading processes (infection, distance)

**BFS Using a Queue**

```
from collections import deque

def bfs(root):

    queue = deque([root])

    while queue:
        node = queue.popleft()
        print(node.value)

        if node.left:
            queue.append(node.left)

        if node.right:
            queue.append(node.right)
```

_BFS doesn’t backtrack because it explores all possibilities in parallel_

Walkthrough:

```
Problem:
Explore level by level.
Find the shortest path when every edge costs the same.

Mental Picture:
A
B C
D E F

State:
queue - Nodes waiting to be explored.
visited

Core Operation:
queue.append(neighbour)
```

Example

```
        A
      /   \
     B     C
    / \     \
   D   E     F
```

Possible DFS traversal: A → B → D → E → C → F

BFS traversal: A → B → C → D → E → F

Example comparison

**Shortest path length only**

```
queue = deque([(start, 0)])
```

**Shortest path (actual route)**

```
queue = deque([(start, [start])])
```

**More efficient (real-world)**

```
parent[child] = parent
```

### Find the shortest path in a grid

Checklist:

For shortest path in a grid, ask:

1. What is a node?
2. What are valid neighbors?
3. What blocks movement?
4. What counts as one step?
5. What do I track in the queue?

For this problem:

1. a cell
2. up/down/left/right cells
3. walls (#)
4. moving to one adjacent cell
5. position + path or position + distance

Code implementation:

```
from collections import deque

def bfs_shortest_path(grid, start, goal):

    queue = deque([(start, [start])])
    visited = set([start])

    while queue:
        node, path = queue.popleft()

        if node == goal:
            return path

        for neighbor in get_neighbors(grid, node):
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append((neighbor, path + [neighbor]))

    return path

def get_neighbors(grid, node):
    rows = len(grid)
    cols = len(grid[0])

    r, c = node

    directions = [
        (-1, 0),  # up
        (1, 0),   # down
        (0, -1),  # left
        (0, 1)    # right
    ]

    neighbors = []

    for dr, dc in directions:
        new_r = r + dr
        new_c = c + dc

        # Check bounds
        if 0 <= new_r < rows and 0 <= new_c < cols:

            # Check not a wall
            if grid[new_r][new_c] != '#':
                neighbors.append((new_r, new_c))

    return neighbors
```

## Key Principle

BFS marks visited when **ENQUEUING (adding)**

DFS marks visited when **VISITING (popping/processing)**

This is a classic distinction.

|                    | DFS                                        | BFS                                                                  |
| ------------------ | ------------------------------------------ | -------------------------------------------------------------------- |
| **Problem**        | Explore everything reachable               | Explore level by level; shortest path in an unweighted graph         |
| **Data Structure** | Stack (or recursion)                       | Queue                                                                |
| **Exploration**    | Go as deep as possible before backtracking | Explore all neighbours at the current distance before moving farther |
