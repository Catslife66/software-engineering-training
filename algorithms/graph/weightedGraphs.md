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

## Drill

Graph:

```
        4
    A ------ B
    |        |
  2 |        | 5
    |        |
    C ------ D
      1    8
```

Adjecency list:

```
graph = {
    "A": [("B", 4), ("C", 2)],
    "B": [("D", 5)],
    "C": [("B", 1), ("D", 8)],
    "D": []
}
```

Start node:

```
A
```

```
Process A:
For B:
best_cost = {
    "A": 0,
    "B": 4,
    "C": ,
    "D":
}
min_heap = [(4, "B")]

For C:
best_cost = {
    "A": 0,
    "B": 4,
    "C": 2,
    "D":
}
min_heap = [(2, "C"), (4, "B")]

Process C:
For B:
best_cost = {
    "A": 0,
    "B": 3,
    "C": 2,
    "D":
}
min_heap = [(3, "B"), (4, "B")]

For D:
best_cost = {
    "A": 0,
    "B": 3,
    "C": 2,
    "D": 10
}
min_heap = [(3, "B"), (4, "B"), (10, "D")]

Process B:
best_cost = {
    "A": 0,
    "B": 3,
    "C": 2,
    "D": 8
}
min_heap = [(4, "B"), (8, "D"), (10, "D")]

Process B:
skip
best_cost = {
    "A": 0,
    "B": 3,
    "C": 2,
    "D": 8
}
min_heap = [(8, "D"), (10, "D")]

Process D:
skip
best_cost = {
    "A": 0,
    "B": 3,
    "C": 2,
    "D": 8
}
min_heap = [(10, "D")]

Process D:
skip
best_cost = {
    "A": 0,
    "B": 3,
    "C": 2,
    "D": 8
}
min_heap = []
```
