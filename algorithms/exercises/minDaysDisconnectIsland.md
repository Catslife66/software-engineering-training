# Minimum Number of Days to Disconnect Island - 1568 HARD

You are given an m x n binary grid grid where 1 represents land and 0 represents water. An island is a maximal 4-directionally (horizontal or vertical) connected group of 1's.

The grid is said to be connected if we have exactly one island, otherwise is said disconnected.

In one day, we are allowed to change any single land cell (1) into a water cell (0).

Return the minimum number of days to disconnect the grid.

Example 1:

```
Input: grid = [[0,1,1,0],[0,1,1,0],[0,0,0,0]]

Output: 2
Explanation: We need at least 2 days to get a disconnected grid.
Change land grid[1][1] and grid[0][2] to water and get 2 disconnected island.
```

Example 2:

```
Input: grid = [[1,1]]
Output: 2
Explanation: Grid of full water is also disconnected ([[1,1]] -> [[0,0]]), 0 islands.
```

Constraints:

```
m == grid.length
n == grid[i].length
1 <= m, n <= 30
grid[i][j] is either 0 or 1.
```

Solution:

```
def dfs(r, c):
    if out_of_bound:
        return
    if water cell:
        return
    if visited cell:
        return

    visited.add((r, c))
    dfs(up)
    dfs(down)
    dfs(left)
    dfs(right)

def count_island():
    visited = set()
    island_count = 0

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == 1 and (r, c) not visited:
                island += 1
                dfs(r, c)

    return island_count

if count_islands() != 1:
    return 0

for r in range(rows):
    for c in range(cols):
        if grid[r][c] == 1:
            grid[r][c] = 0

            if count_islands() != 1:
                return 1

            grid[r][c] = 1
return 2

```

❓How do we know we can disconnect in at most 2 actions?

```
"Cutting the corner" is exactly how top competitive programmers spot this.
They look at the geometrical constraints of a 4-directionally connected grid.

Because you can only move Up, Down, Left, and Right, every single cell in a grid has a maximum of 4 neighbors.

- An internal cell has 4 neighbors (requires 4 cuts to isolate).

- An edge cell has 3 neighbors (requires 3 cuts to isolate).

- A corner cell has only 2 neighbors (requires 2 cuts to isolate).

Since every valid island must have at least one corner or outermost edge tip, you can always target that specific corner cell and destroy its 2 neighbors to disconnect it from the rest of the landmass. The only exception is if the island only has 1 or 2 cells total, which are handled perfectly by Day 1 or Day 0 checks anyway!
```

Hints:

```
Hint 1: Return 0 if the grid is already disconnected.

Use a standard island-counting DFS. Count how many islands are in the grid right now.
If the count is already 0 or greater than 1, return 0.

Hint 2: Return 1 if changing a single land to water disconnect the island.

If it's not already disconnected, you need to test if removing just one cell works.

How do you test this? You can literally try turning a single land cell into water (grid[r][c] = 0), and then re-count the islands.
If the island count changes to 0 or >1, you found a critical cell! Return 1.

Remember to change the cell back to 1 before testing the next cell!

Hint 3: Otherwise return 2.

Hint 4: We can disconnect the grid within at most 2 days.

If you tried removing every single land cell one by one, and none of them successfully disconnected the grid,
then by the law of Hint 4... return 2.
```
