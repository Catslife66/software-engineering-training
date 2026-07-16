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

## Graph Representation

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

### Code Skeleton

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

## Valid Path - A reachability question

Imagine this graph:

```
A ----- B ----- D

 \      |
  \     |
    C --E
```

Adjacency list:

```
graph = {
    "A": ["B", "C"],
    "B": ["A", "D", "E"],
    "C": ["A", "E"],
    "D": ["B"],
    "E": ["B", "C"]
}
```

Question:

Is there a path from A to D?

Solution:

```
1. What is one state?
node

2. What does dfs(state) represent?
explore every node reachable from this node.

3. What is the contract? / What question is this recursive call answering?
From this node, can I reach the target?

4. What should dfs() return?
bool

5. What is the base case?
if node == target:
    return True

6. What does visited represent?
The nodes have been explored

7. What is the recursive step?
Ask each neighbour whether it can reach the target.

8. What happens if every recursive call fails?
Can you reach the target?
neighbour 1: no
neighbour 2: no
neighbour 3: no
...
return False
```

Code skeleton:

```
def dfs(node):

    if node == target:
        return True

    visited.add(node)

    for neighbour in graph[node]:
        if neighbour not in visited:
            if dfs(neighbour):
                return True

    return False
```

Notice something?

This is almost identical to ordinary Graph DFS.

The only new thing is that the function now returns a boolean.

## Clone Graph

Imagine this graph.

```
A ----- B
|       |
|       |
C -------
```

Question:

Make a completely new graph that looks exactly the same.

Suppose a node is:

```
class Node:
    def __init__(self, val):
        self.val = val
        self.neighbors = []
```

One node contains:

- a value
- a list of neighbor pointers

Conceptually:

```
Already cloned → return the existing clone
Not cloned yet → create, complete, and return a new clone
store mapping immediately
clone each neighbour
attach neighbour clones
return current clone
```

Solution:

```
1. What is one state?
node

2. What does dfs(state) represent?
Return the clone of this node after cloning all nodes reachable from it.

3. What is the contract?
Given this original node, return its corresponding cloned node.

4. What should dfs() return?
node

5. What is the base case?
The existing cloned node corresponding to this original node.
if node in clones:
    return clones[node]

6. What information must we remember?
mapping
original node → cloned node

This mapping replaces a normal visited set because it answers both:
Has this node been cloned?
Which cloned object belongs to it?

7. What is the recursive step?
For each original neighbour, obtain its clone recursively and attach that clone to the current cloned node.

8. When the current node finishes, what should it return to its caller?
The cloned current node
```

Code skeleton:

```
def dfs(node):
    if node in clones:
        return clones[node]
    clone = Node(node.val)
    clones[node] = clone

    for neighbour in node.neighbours:
        clone.neighbours.append(dfs(neighbours))

    return clone
```

## Directed Cycle Detection

Imagine this graph:

```
A → B → C
    ↑     |
    └─────┘
```

- Visited = nodes we've discovered at least once.
- Call stack = DFS calls that are still active and haven't returned yet.

Walkthrough:

dfs(A)

```
visited: {A}
Call Stack:

┌─────────┐
│ dfs(A)  │
└─────────┘
```

dfs(B)

```
visited: {A, B}
Call Stack:
┌─────────┐
│ dfs(B)  │   ← currently executing
├─────────┤
│ dfs(A)  │   ← waiting
└─────────┘
```

Both A and B are still "active."

dfs(C)

```
visited: {A, B, C}

Call stack:
┌─────────┐
│ dfs(C)  │ ← running
├─────────┤
│ dfs(B)  │
├─────────┤
│ dfs(A)  │
└─────────┘

```

Now C sees an edge: C -> B, Should that immediately tell us we've found a cycle?

Yes, because B is visited and on current call stack

Visualise it

```
Enter node
    ↓
visited.add(node)
    ↓
path.add(node)
    ↓
Explore neighbours
    ↓
path.remove(node)
    ↓
Return
```

## Undirected Cycle Detection

> Cycle detection is basically DFS plus one extra piece of information.

Imagine graph A:

```
A ----- B
|       |
|       |
C ------
```

Question:

Can you start from A...

...follow the edges...

...and eventually return to A without retracing the same edge backwards immediately?

Yes.

For example:

```
A → B → C → A
```

That is called a **cycle**.

Now look at graph B.

```
A
|
B
|
C
```

Can you keep following edges and come back to where you started?

No.

There is no cycle.

Why graph B is not a cycle?

DFS path:

```
A → B → C
```

At C, neighbors are:

```
B
```

We already know B is visited.

Because node B is the node we just came from.

If we treated that as a cycle, then every single edge in an undirected graph would look like a cycle.

That can't be right.

Why graph A is a cycle?

DFS path:

```
A → B → C
```

At C, neighbors are:

```
B
A
```

Seeing B is expected.

- It's your parent.
- You literally walked from B to C.

Seeing A is different.

- You didn't come from A.
- Yet A is already visited.

That means you've found **another path** back to a previously explored node.

The missing information

Until today, DFS only remembered:

```
visited
```

Now we need one more piece of information.

When we're at:

```
C
```

we want to know:

_Which node did I come from?_

Not because we need it for traversal...

...but because we need it to ignore the edge back to the parent.

That extra information is simply:

```
parent
```

**Visualize it**

Instead of thinking:

```
A → B → C
```

Think like this:

```
Current: C
Parent : B
```

Now if you inspect C's neighbors:

```
Neighbor B  ← ignore (it's the parent)
Neighbor A  ← visited and NOT the parent → cycle!
```

That one comparison is the heart of cycle detection in an **undirected graph**.

### Code Skeleton

Concept:

```
1. What should the function signature become?
dfs(node, parent)

2. Conceptually, what does that new parameter represent?
The node from which we entered the current node.
```

```
def dfs(node, parent):
    visited.add(node)

    for neighbour in graph[node]:

        if neighbour not in visited:
            if dfs(neighbour, node):
                return True

        elif neighbour != parent:
            return True

    return False
```

### This is an invariant

> An invariant is something that stays true throughout the algorithm.

For DFS, one invariant is:

```
Every node in visited has already been explored.
```

For BFS, one invariant is:

```
Every node in the queue has been discovered but not yet processed.
```

For cycle detection, we add another invariant:

```
The parent is the node we used to enter the current node.
```

Notice what happened.

We didn't invent a new algorithm.

We added one more invariant.

| Problem              | Traversal | New Invariant                     |
| -------------------- | --------- | --------------------------------- |
| Graph DFS            | DFS       | visited                           |
| Graph BFS            | BFS       | queue = discovered, not processed |
| Connected Components | DFS       | outer loop + count                |
| Undirected Cycle     | DFS       | parent                            |
| Directed Cycle       | DFS       | visited + path                    |

## Your Graph Toolbox

**Question 1**

Do I need to visit everything?

If yes...

Think:

```
DFS
or
BFS
```

Example:

- Traverse graph
- Print all nodes
- Explore friends
- Explore network

**Question 2**

Am I counting groups?

Think:

```
Connected Components
```

Example:

```
A - B

C - D

E
```

Answer:

```
3 components
```

Pattern:

```
Outer loop
+
DFS
```

**Question 3**

Am I checking if something loops forever?

Think:

```
Cycle Detection
```

Extra invariant:

```
parent
```

**Question 4**

Am I scheduling tasks with dependencies?

Think:

```
Topological Sort
```

Extra invariant:

```
indegree
```

| Question            | Engine  | Extra State        |
| ------------------- | ------- | ------------------ |
| Traverse graph      | DFS/BFS | visited            |
| Is there a path?    | DFS/BFS | target             |
| Count friend groups | DFS     | outer loop + count |
| Detect cycle        | DFS     | parent             |
| Course schedule     | BFS     | indegree           |
| Shortest path       | BFS     | distance           |

### Checkpoints

1. DFS or BFS?
2. What is the extra invariant/state?

Problem A

"Find the shortest number of flights from London to Tokyo."

1. BFS
2. distance

Problem B

"Print every employee under a manager in an organizational chart."

1. DFS or BFS
2. DFS or BFS + visited/children traversal

Problem C

"Can every package in this build system be compiled?"

Each package depends on some others.

1. BFS
2. indegree

Problem D

"How many disconnected computer networks are there?"

1. DFS
2. DFS + outer loop + count

Problem E

"Starting from one user, can I eventually reach Elon through friend connections?"

1. DFS or BFS
2. DFS or BFS + visited + target

## DFS Design Checklist

1. What does one state represent?

- (r, c)?
- node?

2. What does dfs(state) represent?

- Explore?
- Count?
- Search?

3. What should dfs() return?

- Nothing?
- True/False?
- An integer?
- A list?

4. What is the base case?
5. What does visited represent?
6. How do I get neighbors?
7. What is the recursive step?
