# Count Sub Islands - 1905

You are given two m x n binary matrices grid1 and grid2 containing only 0's (representing water) and 1's (representing land). An island is a group of 1's connected 4-directionally (horizontal or vertical). Any cells outside of the grid are considered water cells.

An island in grid2 is considered a sub-island if there is an island in grid1 that contains all the cells that make up this island in grid2.

Return the number of islands in grid2 that are considered sub-islands.

Example 1:

```
Input: grid1 = [[1,1,1,0,0],[0,1,1,1,1],[0,0,0,0,0],[1,0,0,0,0],[1,1,0,1,1]], grid2 = [[1,1,1,0,0],[0,0,1,1,1],[0,1,0,0,0],[1,0,1,1,0],[0,1,0,1,0]]Output: 3Explanation: In the picture above, the grid on the left is grid1 and the grid on the right is grid2.

The 1s colored red in grid2 are those considered to be part of a sub-island. There are three sub-islands.
```

Example 2:

```
Input: grid1 = [[1,0,1,0,1],[1,1,1,1,1],[0,0,0,0,0],[1,1,1,1,1],[1,0,1,0,1]], grid2 = [[0,0,0,0,0],[1,1,1,1,1],[0,1,0,1,0],[0,1,0,1,0],[1,0,0,0,1]]
Output: 2
Explanation: In the picture above, the grid on the left is grid1 and the grid on the right is grid2.
The 1s colored red in grid2 are those considered to be part of a sub-island. There are two sub-islands.
```

Constraints:

```
m == grid1.length == grid2.length
n == grid1[i].length == grid2[i].length
1 <= m, n <= 500
grid1[i][j] and grid2[i][j] are either 0 or 1.
```

Solution 1:

```
class Solution:
    def countSubIslands(self, grid1: List[List[int]], grid2: List[List[int]]) -> int:
        rows = len(grid2)
        cols = len(grid2[0])
        visited = set()

        def dfs(r, c):
            nonlocal is_sub
            if r < 0 or r >= rows or c < 0 or c >= cols:
                return
            if grid2[r][c] == 0:
                return
            if (r, c) in visited:
                return

            visited.add((r, c))
            if grid2[r][c] != grid1[r][c]:
                is_sub = False

            dfs(r+1, c)
            dfs(r-1, c)
            dfs(r, c+1)
            dfs(r, c-1)

        count = 0
        for r in range(rows):
            for c in range(cols):
                if grid2[r][c] == 1 and (r, c) not in visited:
                    is_sub = True
                    dfs(r, c)
                    if is_sub:
                        count += 1

        return count

```

Solution 2:

```

class Solution:
    def countSubIslands(self, grid1: List[List[int]], grid2: List[List[int]]) -> int:
        rows = len(grid2)
        cols = len(grid2[0])
        visited = set()

        def dfs(r, c):
            if r < 0 or r >= rows or c < 0 or c >= cols:
                return True
            if grid2[r][c] == 0:
                return True
            if (r, c) in visited:
                return True

            visited.add((r, c))

            # Check if this specific cell is valid
            # It's only a sub-island match if grid1 is ALSO land
            current_match = (grid1[r][c] == 1)

            # CRITICAL: Run all 4 directions completely on separate lines
            # This avoids the short-circuiting trap!
            down_match  = dfs(r + 1, c)
            up_match    = dfs(r - 1, c)
            right_match = dfs(r, c + 1)
            left_match  = dfs(r, c - 1)

            # This whole component is a sub-island ONLY if the current cell
            # matches AND every single neighbor traversal matches
            return current_match and down_match and up_match and right_match and left_match

        count = 0
        for r in range(rows):
            for c in range(cols):
                # If we find a fresh piece of island in grid2
                if grid2[r][c] == 1 and (r, c) not in visited:
                    # The function itself directly tells us True or False
                    if dfs(r, c):
                        count += 1

        return count
```
