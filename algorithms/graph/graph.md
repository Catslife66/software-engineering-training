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
| Cycle Detection      | DFS       | parent                            |

## Topological Sort

Topological Sort usually **does not have one unique answer**.

It has:

> Any ordering that satisfies all dependencies.

That's a very different goal from shortest path.

We're not looking for the "best" path.

We're looking for a valid ordering.

```
In-degree of a node = the number of edges pointing into it.
```

BFS asks:

```
Which nodes have been discovered?
```

Topological Sort asks:

```
Which nodes are ready?
```

Those "ready" nodes all have:

```
in-degree = 0
```

Remember Rotting Oranges?

Initially the queue contained:

```
All rotten oranges.
```

Why?

Because they were ready to spread.

Now look at Topological Sort.

Initially the queue contains:

```
All nodes with in-degree = 0.
```

Why?

Because they are ready to be processed.

Notice the pattern?

We're using a queue again.

But the meaning of the queue has changed.

| Problem          | Queue contains                              |
| ---------------- | ------------------------------------------- |
| Shortest Path    | Frontier nodes                              |
| Rotting Oranges  | Rotten oranges that spread next             |
| Graph BFS        | Discovered but unprocessed nodes            |
| Topological Sort | Nodes whose prerequisites are all satisfied |

Graph:

```

graph = {
    "Programming": ["Data Structures", "Databases"],
    "Data Structures": ["Algorithms"],
    "Algorithms": [],
    "Databases": []
}
```

Initial indegree:

```
Programming      0
Data Structures  1
Algorithms       1
Databases        1
```

Initial queue:

```
[Programming]
```

Result so far:

```
[]
```

Walkthrough:

Process: Programming

```
1. result adds:
Programming

2. affected neighbours:
Data Structures, Databases

3. indegrees:
Data Structures: 1 → 0
Databases: 1 → 0
Algorithms: still 1

4. ready nodes:
Data Structures, Databases

5. queue = [Data Structures, Databases]

6. result = [Programming]
```

Process: Data Structures

```
1. What gets added to the result?
Data Structures

2. Which neighbor is affected?
Algorithms

3. What happens to its in-degree?
Databases: still 0
Algorithms: 1 -> 0

4. Does any new node become ready?
Algorithms

5. What is the queue now?
queue = [Databases, Algorithms]

6. What is the result now?
result = [Programming, Data Structure]
```

Process: Databases

```
1. What gets added to the result?
Databases

2. Which neighbors are affected?
None

3. What happens to the queue?
queue = [Algorithms]

4. What is the result?
result = [Programming, Data Structure, Databases]
```

Process: Algorithms

```
Add Algorithms to the result.
It has no neighbors.
Queue becomes empty.

Final result:
Programming
Data Structures
Databases
Algorithms
```

And that's a valid topological ordering.

### This is another invariant

**Graph BFS**

Invariant:

```
Every node in the queue
has been discovered
but not processed.
```

**Topological Sort**

Invariant:

```
Every node in the queue
has indegree = 0.
```

or, in plain English:

```
Every node in the queue
has no unfinished prerequisites.
```

### Code Skeleton

```
from collections import deque

def topo_sort(graph):
    indegree = {}

    for node in graph:
        indegree[node] = 0

    for node in graph:
        for neighbour in graph[node]:
            indegree[neighbour] += 1

    queue = deque()

    for node in graph:
        if indegree[node] == 0:
            queue.append(node)

    result = []

    while queue:
        node = queue.popleft()
        result.append(node)

        for neighbour in graph[node]:
            indegree[neighbour] -= 1

            if indegree[neighbour] == 0:
                queue.append(neighbour)

    return result
```

## Cycle Detection in Topological Sort

Graph:

```
A → B
B → C
C → A
```

Concept:

```
1. What is the indegree of A?
1
2. What is the indegree of B?
1
3. What is the indegree of C?
1
4. Which nodes have indegree 0 initially?
None
5. What will the initial queue contain?
None
6. Why is that a problem?
So there is no course/task that is ready to start.
```

After topological sort finishes:

```
if len(result) != len(graph):
    return "cycle detected"
```

Why?

Because some nodes never became ready.

In this graph:

```
A → B → C → A
```

Result stays:

```
[]
```

But graph has:

```
3 nodes
```

So:

```
len(result) != len(graph)
```

means:

```
some nodes are trapped in dependency cycle
```

```
def topo_sort(graph):
    if len(result) != len(graph):
        return None  # cycle exists

    return result
```

Topological sort only works on a graph with no directed cycle.

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
