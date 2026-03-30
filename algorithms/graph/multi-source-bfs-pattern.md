# Multi-source BFS

> Many starting points → expand together

This is used in problems like:

- nearest hospital
- nearest gate
- spread of infection
- rotten oranges
- distance to nearest zero

It is the same BFS idea, but instead of starting from one source, we start from many sources at once.

```
Instead of: “for each node → search outward”

We do: “from all sources → expand outward once” -> avoid running BFS MANY times

O(n²) → optimal
```

### When to recognize this pattern

Whenever you see:

```
“nearest X”
“distance to closest Y”
“spread from multiple points”
```

Think:

```
multi-source BFS
```
