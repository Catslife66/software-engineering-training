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
