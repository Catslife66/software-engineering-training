# Graph

Imagine this map:

```
A ----- B
|       |
|       |
C ----- D
```

Question:

Is this a grid?

No.

But can DFS work on it?

Yes.

Can BFS work on it?

Yes.

Why?

Because DFS and BFS don’t actually care about grids.

**What is a graph?**

A graph is simply:

```
Nodes
+
Connections between nodes
```

**Grid**

Even a grid is secretly a graph.

For example:

```
A B
C D
```

really means:

```
A -- B
|    |
C -- D
```

Each cell is a node.

Each neighboring move is an edge.

More examples:

**Social network**

```
People = nodes
Friendships = edges
```

**London Underground**

```
Stations = nodes
Railway tracks = edges
```

**Computer network**

```
Computers = nodes
Network cables = edges
```

## Graph Thinking

Look at this grid:

```
A B C
D E F
G H I
```

When you solved grid BFS, what did you do?

For each cell:

```
for dr, dc in directions:
```

There is no such thing as a "grid algorithm."

There is only:

```
Graph traversal.
```

The only difference is:

How do I find the neighbors?

**Grid**

Neighbors are computed.

You literally calculate them:

```
nr = r + dr
nc = c + dc
```

**General graph**

Neighbors are stored.

Instead of computing them, you ask:

```
for neighbor in graph[node]:
```

Every node has a list of neighbors.

```
      A
     / \
    B   C
   / \   \
  D   E   F
```

For each node, store a list of nodes reachable in one step.

Adjacency list:

```
A → [B, C]
B → [A, D, E]
C → [A, F]
D → [B]
E → [B]
F → [C]
```

Python representation:

```
graph = {
    "A": ["B", "C"],
    "B": ["A", "D", "E"],
    "C": ["A", "F"],
    "D": ["B"],
    "E": ["B"],
    "F": ["C"],
}
```

Question:

```
1. What does graph["A"] mean?
A's neighbours
2. What does graph["C"] mean?
C's neighbours
3. In grid DFS/BFS, how did we find neighbours?
for dr, dc in [(0, 1), (0, -1), (1, 0), (-1, 0)]:
    nr = r + dr
    nc = c + dc
4. In graph DFS/BFS, how do we find neighbours?
for neighbour in graph[node]:
    ...
5. What is the big similarity between grid traversal and graph traversal?
DFS and BFS don’t care whether they’re traversing a grid or a graph. They only care how to get the neighbors.
```

**Grid DFS**

```
def dfs(r, c):

    ...

    for dr, dc in directions:
        nr = r + dr
        nc = c + dc

        dfs(nr, nc)
```

**Graph DFS**

```
def dfs(node):

    ...

    for neighbor in graph[node]:
        dfs(neighbor)
```

That’s the whole change.

Everything else is identical.

## Graph DFS

Concept:

```
1. What should dfs(node) represent?
Explore every node reachable from this node.

2. What should the base case be?
if the node is in visited

3. What should visited represent?
Nodes that have already been completely explored.
```

Code skeleton:

```
def dfs(state):

    if base_case:
        return

    mark_visited(state)

    for next_state in neighbors(state):
        dfs(next_state)
```

## Graph BFS

```
from collections import deque

def bfs(node):
    queue.append(node)
    visited.add(node)

    while queue:
        node = queue.popleft()

        for neighbour in graph[node]:
            if neighbour not in visited:
                queue.append(neighbour)
                visited.add(neighbour)
```

## Connected Components

Imagine this graph:

```
A ----- B      F ----- G

|       |             |

C       D             H


E
```

This is Number of Islands, but without the grid.

Clean mapping:

```
Graph connected component = island
Node = land cell
Edge = connection between land cells
Outer loop = scan every possible start
DFS = consume one connected group
count += 1 = found a new group
```

So the structure is:

```
for node in graph:
    if node not in visited:
        count += 1
        dfs(node)
```

Key idea:

```
One DFS explores one connected component.
One count increment happens per new component.
```

## Code Skeleton

Concept:

```
1. What does dfs(node) represent?
Explore every node in the connected component containing node.
2. What does visited represent?
the nodes that have been explored
3. Why does count += 1 happen before dfs(node)?
If this node is not visited,
it cannot belong to any component we've already explored.
Therefore it must be the first node of a new connected component.
```

Code:

```
def count_components(graph):
    visited = set()
    count = 0

    def dfs(node):
        if node in visited:
            return

        visited.add(node)

        for neighbour in graph[node]:
            dfs(neighbour)

    for node in graph:
        if node not in visited:
            count += 1
            dfs(node)

    return count
```
