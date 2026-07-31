# Weighted Graphs

Graph:

```
           100
A ----------------> B
 \
  \1
   \
    C --1--> D --1--> B
```

A **min-heap** stores items so the smallest-cost item is always available at the top.

## Dijkstra’s Algorithm - selects the node with the minimum known cost.

_Who currently has the smallest known cost?_

> When we choose the unprocessed node with the smallest known cost, we know there cannot be a cheaper path to it later.

Start:

```
best_cost = {
    "A": 0,
    "B": float("inf"),
    "C": float("inf"),
    "D": float("inf")
}

min_heap = [(0, "A")]
```

Process A:

```
For B:
best_cost = {
    "A": 0,
    "B": 100,
    "C": float("inf"),
    "D": float("inf")
}
min_heap = [(100, "B")]

For C:
best_cost = {
    "A": 0,
    "B": 100,
    "C": 1,
    "D": float("inf")
}
min_heap = [(1, "C"), (100, "B")]
```

Process C:

```
best_cost = {
    "A": 0,
    "B": 100,
    "C": 1,
    "D": 2
}
min_heap = [(2, "D"), (100, "B")]
```

Process D:

```
best_cost = {
    "A": 0,
    "B": 3,
    "C": 1,
    "D": 2
}
min_heap = [(3, "B"), (100, "B")]
```

Concept

```
Problem:
Find the minimum-cost path from a start node to every other node.

State:
best_cost[node]
The best known cost from the start to every node.

Frontier:
Discovered nodes whose shortest path has not yet been finalised. / Candidates waiting to be explored.

Data Strucure:
Priority Queue
Always gives us the cheapest discovered route next.

Initialisation:
everyone else = ∞

Update rule:
update the cost if a cheaper cost

Invariant:
Once a node leaves the priority queue, no cheaper path to that node can ever be found.
```

Code Skeleton

```
import heapq

def dijkstra(graph, start):
    best_cost = {
        node: float("inf")
        for node in graph
    }

    best_cost[start] = 0

    heap = []
    heapq.heappush(heap, (0, start))

    while heap:
        current_cost, current_node = heapq.heappop(heap)
        if current_cost > best_cost[current_node]:
            continue

        for neighbour, weight in graph[current_node]:
            new_cost = current_cost + weight

            if new_cost < best_cost[neighbour]:
                best_cost[neighbour] = new_cost
                heapq.heappush(min_heap, (new_cost, neighbour))

    return best_cost
```

## Bellman-Ford

With non-negative weights, any route that continues through another node can only become more expensive.

With a negative edge, a later route can suddenly reduce the cost.

Graph:

```
        5
    A ------> B
    |         |
   2|         |1
    |         |
    v         v
    C ------> D
        4

edge list:
1. A → B (5)
2. A → C (2)
3. B → D (1)
4. C → D (4)

initially:
best_cost[A] = 0
best_cost[B] = ∞
best_cost[C] = ∞
best_cost[D] = ∞

Read edge 1: A -> B
A = 0
B = 5
C = ∞
D = ∞

Read edge 2: A -> C
A = 0
B = 5
C = 2
D = ∞

Read edge 3: B -> D
A = 0
B = 5
C = 2
D = 6

Read edge 4: C -> D
A = 0
B = 5
C = 2
D = 6

This is one complete pass:
Read edge 1
↓
Read edge 2
↓
Read edge 3
↓
Read edge 4

A pass is reading the edge list from beginning to end.
```

Precise meaning of a pass

```
Pass 1:
scan every edge once

Pass 2:
scan every edge once again

Pass 3:
scan every edge once again
```

Concept

```
Problem:
Find the minimum-cost path from a start node to every other node.

Optimisation Objective:
Minimise total path cost.

State:
best_cost[node]
The best known cost from the start to every node.

Initialisation:
A = 0
B = ∞
C = ∞
D = ∞

Update Rule:
Same relaxation in Dijkstra

Termination rule:
If an entire pass produces no updates,
all reachable shortest-path costs are settled.

```

**Why does Bellman-Ford detect negative cycles?**

This is something Dijkstra cannot do.

Graph:

```
        4
    A ------> B
    ^         |
    |         |
   -6         | 1
    |         |
    |         v
    C <-------
        1

Edges:
A → B = 4
B → C = 1
C → A = -6
```

Core rule:

```
Normal passes:
find shortest paths

One extra pass:
check whether any cost still improves

If an improvement still happens:
negative cycle detected
```

Walkthrough:

```
1. Track best known cost to each node
2. Scan every edge
3. Relax an edge when it improves a cost
4. Repeat the edge scan
5. Stop early if a full pass changes nothing
6. After the normal passes, scan once more
7. If anything still improves, a reachable negative cycle exists
```

Code Skeleton:

```
Step 1:
# initiate best_cost
best_cost = {
    node: float('inf') for node in graph
}
best_cost[start] = 0

Step 2:
# Repeat several passes
for _ in range(number_of_nodes - 1):

Step 3:
# Inside each pass
for from_node, to_node, weight in edges:

Step 4:
# For each edge
candidate = best_cost[from_node] + weight

Step 5:
# If the candidate is better
best_cost[to_node] = candidate


for _ in range(number_of_nodes - 1):
    changed = False

    for from_node, to_node, weight in edges:
        candidate = best_cost[from_node] + weight

        if candidate < best_cost[to_node]:
            best_cost[to_node] = candidate
            changed = True

    if not changed:
        break


```

Suppose the longest shortest path in a graph contains 3 edges.

Information can only travel:

- Pass 1: one edge
- Pass 2: two edges
- Pass 3: three edges

After that, every reachable shortest path has had enough passes for its information to propagate all the way to the destination.

This is why Bellman-Ford runs **at most**:

```
number_of_nodes - 1
```

```
def bellman_ford(number_of_nodes, edges, start):

    best_cost = {
        node: float('inf') for node in range(number_of_nodes)
    }
    best_cost[start] = 0

    for _ in range(number_of_nodes - 1):
        changed = False
        for from_node, to_node, weight in edges:
            candidate = best_cost[from_node] + weight

            if candidate < best_cost[to_node]:
                best_cost[to_node] = candidate
                changed = True

        if not changed:
            break
```
