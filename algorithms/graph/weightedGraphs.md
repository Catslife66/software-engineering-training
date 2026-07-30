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
1. B → D
2. C → D
3. A → B
4. A → C

A pass is:
Read edge 1
↓
Read edge 2
↓
Read edge 3
↓
Read edge 4
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

Update Rule
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
