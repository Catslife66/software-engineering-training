# Topological Sort

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

## This is another invariant

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

## Kahn’s Algorithm - Course Schedule I

_Can I finish all courses?_

```
Kahn’s Algorithm repeatedly processes tasks whose dependencies have all been satisfied. Completing a task satisfies one dependency for every task that depends on it, which may make additional tasks ready to process. This continues until no more tasks are ready. If every task is processed, the dependency graph is valid. If some tasks remain, they are waiting on each other through a dependency cycle, so no valid ordering exists.
```

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

Kahn’s Algorithm needs three core structures:

```
1. adjacency list
2. in-degree count
3. queue
```

Code Skeleton

```
from collections import deque
from typing import List


class Solution:
    def canFinish(self, numCourses: int, prerequisites: List[List[int]]) -> bool:
        graph = [[] for _ in range(numCourses)]
        indegree = [0] * numCourses

        for course, prerequisite in prerequisites:
            graph[prerequisite].append(course)
            indegree[course] += 1

        queue = deque()

        for course in range(numCourses):
            if indegree[course] == 0:
                queue.append(course)

        processed = 0

        while queue:
            course = queue.popleft()
            processed += 1

            for next_course in graph[course]:
                indegree[next_course] -= 1

                if indegree[next_course] == 0:
                    queue.append(next_course)

        return processed == numCourses
```

## Course Schedule II

_If I can finish them, what order should I take them?_

Code Invariant

```
order = []

...
while queue:
    course = queue.popleft()
    order.append(course)
    processed += 1

...

return order if len(order) == numCourses else []
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
