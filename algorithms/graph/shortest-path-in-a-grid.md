# Find the shortest path

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

## The reusable shortest-path checklist

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

## Two common BFS versions

**Version 1: track full path**

Easy to understand.

Queue stores:

```
(node, path)
```

**Version 2: track distance only**

More common when only the step count is needed.

Queue stores:

```
(node, steps)
```
