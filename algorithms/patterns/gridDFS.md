# Grid DFS

Grid DFS is basically:

```
tree DFS + boundary checks + visited tracking
```

Instead of:

```
node.left / node.right
```

we use directions:

```
up, down, left, right
```

**Key mindset shift**

In trees:

```
structure already prevents cycles
```

In grids/graphs:

```
cycles are everywhere
```

## Drill 1 — Number of Islands

> DFS returns nothing. outer loop counts how many times we start DFS on new unvisited land.

Problem

```
grid = [
  ["1","1","0"],
  ["0","1","0"],
  ["1","0","1"]
]

1 = land
0 = water
```

Return the number of islands.

Connected horizontally/vertically.

Solution:

```
1. What does dfs(r, c) represent?
Explore the entire connected island starting from cell (r, c).

2. Base cases?
if r < 0 or r >= rows or c < 0 or c >= cols:
    return

if grid[r][c] == "0":
    return

if (r, c) in visited:
    return

3. Recursive directions?
visited.add((r, c))
dfs(r + 1, c)
dfs(r - 1, c)
dfs(r, c + 1)
dfs(r, c - 1)

4. When do we increase island count?
When we find an unvisited land cell and start a NEW dfs. Because one DFS completely explores ONE island.

if grid[r][c] == "1" and (r, c) not in visited:
    islands += 1
    dfs(r, c)

5. Why do we need visited?
DFS would revisit the same land forever
```

Code implementation:

```
// DEF explores from one starting point
def dfs(r, c):
    if r < 0 or r >= rows or c < 0 or c >= cols:
        return

    if grid[r][c] == "0":
        return

    if (r, c) in visited:
        return

    visited.add((r, c))

    dfs(r + 1, c)
    dfs(r - 1, c)
    dfs(r, c + 1)
    dfs(r, c - 1)

// the outer loop does the scanning/counting - find starting points

islands = 0
visited = set()

for r in range(rows):
    for c in range(cols):
        if grid[r][c] == "1" and (r, c) not in visited:
            islands += 1
            dfs(r, c)
```

Pattern:

```
DFS purpose:
consume one island

Returns:
nothing

Outer loop:
find island starts

Result:
number of islands
```

## Drill 2 - Largest island size

> DFS returns the size of that island, and outer loop finds island starts and tracks the maximum size.

Problem

```
1 1 0 0
0 1 0 1
1 0 0 1
```

Return the size of the largest island.

Solution:

```
1. What should dfs(r, c) return?
return number of reachable land cells starting from (r,c)

2. Three base cases in the correct order
// out of bounds contributes 0
if r < 0 or r >= rows or c < 0 or c >= cols:
    return 0

// water contributes 0
if grid[r][c] == 0:
    return 0

3. Recursive step:
//land contributes 1 + neighbors
return 1 + dfs(down) + dfs(up) + dfs(right) + dfs(left)

4. dfs code structure
def dfs(r, c):
    if r < 0 or r >= rows or c < 0 or c >= cols:
        return 0

    if grid[r][c] == 0:
        return 0

    if (r, c) in visited:
        return 0

    visited.add((r, c))

    return (
        1
        + dfs(r + 1, c)
        + dfs(r - 1, c)
        + dfs(r, c + 1)
        + dfs(r, c - 1)
    )
```

Pattern:

```
DFS purpose:
measure one island

Returns:
size

Outer loop:
find island starts
track max
```

## Drill 3 - Flood Fill

> Flood Fill changes one connected region, not every matching color in the whole grid.

Problem

You are given an image grid:

```
1 1 1
1 1 0
1 0 1
```

Start cell:

```
sr = 1, sc = 1
```

New color:

```
2
```

Change the connected region with the same original color as the start cell.

Expected output:

```
2 2 2
2 2 0
2 0 1
```

Solution:

```
1. What does dfs(r, c) represent?
exploring connected region from (r,c) and change its original colour to 2

2. What is originalColor?
originalColor = image[sr][sc]

3. Base cases?
if out of bounds:
    return

if grid[r][c] != originalColor:
    return

4. What do we do to a valid cell?
grid[r][c] = newColor

5. Why do we NOT need a visited set if we mutate the grid?
once color changes
we know it has already been processed
```

Code implementation:

```
originalColour = image[sr][sc]

if originalColour == newColour:
    return image

def dfs(r, c):
    if r < 0 or r >= rows or c < 0 or c >= cols:
        return

    if image[r][c] != originalColour:
        return

    image[r][c] = newColour

    dfs(r + 1, c)
    dfs(r - 1, c)
    dfs(r, c + 1)
    dfs(r, c - 1)

dfs(sr, sc)
return image
```

Key rule:

```
Number of Islands → outer loop scans every cell
Flood Fill → start from one cell only
```

Pattern:

```
DFS purpose:
recolor region

Returns:
nothing

Outer loop:
none
```

## Drill 4 - Island Perimeter

Problem

```
1 1
1 0
```

Return the perimeter of an island

Solution:

```
1. What dfs(r,c) represent?
exploring from (0,0),
visiting all connected land,
returning number of exposed edges around connected land

2. Which edges contribute to perimeter?
(0,0) → up + left = 2
(0,1) → up + right + bottom = 3
(1,0) → left + bottom + right = 3

3. What should one land cell contribute?
A land cell itself DOES NOT automatically contribute 4
Instead DFS asks what happens on each side?
For each direction up/down/left/right, we check is this edge exposed?

4. What should water contribute?
contribute 1 side to perimeter

5. What should out-of-bounds contribute?
contribute 1 side to perimeter

6. Why is perimeter NOT the same as island size?
Size: count cells
Perimeter: count exposed edges

7. Why does visited land return 0?
visited land returns 0 because its perimeter contribution has already been accounted for.

8. What does one valid land cell contribute?
one land cell contributes the perimeter returned by its four sides.
Each side asks does this edge touch water or outside?
If yes +1 perimeter
```

Code implementation:

```
def island_perimeter(grid):
    rows = len(grid)
    cols = len(grid[0])
    visited = set()

    def dfs(r, c):
        # exposed outside edge
        # Did this side of the cell touch outside?
        if r < 0 or r >= rows or c < 0 or c >= cols:
            return 1

        # exposed water edge
        if grid[r][c] == 0:
            return 1

        # already counted this land cell
        if (r, c) in visited:
            return 0

        visited.add((r, c))

        return (
            dfs(r + 1, c)
            + dfs(r - 1, c)
            + dfs(r, c + 1)
            + dfs(r, c - 1)
        )

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == 1:
                return dfs(r, c)

    return 0
```

Pattern:

```
DFS purpose:
measure perimeter

Returns:
perimeter

Outer loop:
find one land start
```

For size problems:

```
water = 0
outside = 0
land = 1
```

For perimeter problems:

```
water = 1
outside = 1
land = ask neighbors

return perimeter_from_neighbors -> because a land cell itself does not contribute perimeter
```

## Drill 5 - Boundary-connected land

Problem:

Return how many land cells are **connected to** the boundary of the grid.

Grid:

```
1 0 1
1 1 0
0 1 1
```

Boundary cells are cells on the outer edge of the grid.

Key idea:

```
grid:

1    0    1
|
1 -- 1    0
     |
0    1 -- 1

The center cell (1,1) is not on the boundary.

But can we walk from (1,1) to a boundary land cell?

(1,1)
↓
(2,1)

Yes.

Or:

(1,1)
←
(1,0)

Yes.

So (1,1) belongs to a land region that touches the boundary.

Therefore:

(1,1)
is boundary-connected

The kep phrase is "connected to"
```

Solution:

```
1. What should the outer loop do?
Outer loop scans the grid boundary.
When it finds boundary land that is not visited, start DFS.

2. Which cells should start DFS?
(0, 0) -> (1,0), (1,1), (2,1), (2,2)
(0, 2)

3. What should dfs(r,c) represent?
explores and marks all land connected to this boundary land cell.

4. Should dfs return anything?
dfs returns number of boundary-connected land cells in this connected region.

5. What should visited prevent?
visited prevents double-counting and infinite revisits.
```

Code implementation:

```
def dfs(r, c):
    if r < 0 or r >= rows or c < 0 or c >= cols:
        return 0
    if grid[r][c] == 0:
        return 0
    if (r, c) in visited:
        return 0
    visited.add((r, c))
    return (1 + dfs(r+1, c) + dfs(r-1, c) + dfs(r, c+1) + dfs(r, c-1))


count = 0
for r in range(rows):
    for c in range(cols):
        is_boundary = (
            r == 0 or
            r == rows - 1 or
            c == 0 or
            c == cols - 1
        )
        if grid[r][c] == 1 and is_boundary:
            count += dfs(r, c)
return count
```

## Drill 6 - Enclosed land

Question:

Return how many land cells are NOT connected to the boundary.

These are often called **enclaves**.

grid:

```
0 0 0 0
0 1 1 0
0 1 0 0
0 0 1 0
```

Key idea:

```
First Start from boundary land.
Mark/Remove boundary-connected land.
Then count remaining land.
```

Walkthrough:

```
Step 1:

Start DFS from boundary land.

(3,2) gets marked.

Visited: (3,2)

Step 2:

Scan the whole grid.

Count: land AND not visited

Remaining: (1,1), (1,2), (2,1)

Answer: 3
```

Key patterns:

```
boundary DFS = mark land that can escape
remaining land = enclosed
```

Solution:

```
1. What does dfs(r,c) represent?
exploring connected lands from cell (r, c) and mark them as visited

2. Does dfs return anything?
No, dfs needs to mark connected lands it has been visited.

3. What does the first boundary scan do?
mark the lands that are connected from the boundary as visited.

4. What does the second full scan do?
explore unvisited lands and mark them as visited.
```

Code implementation:

```
def dfs(r, c):
    if r < 0 or r >= rows or c < 0 or c >= cols:
        return

    if grid[r][c] == 0:
        return

    if (r, c) in visited:
        return

    visited.add((r, c))

    dfs(r + 1, c)
    dfs(r - 1, c)
    dfs(r, c + 1)
    dfs(r, c - 1)

for r in range(rows):
    for c in range(cols):
        if grid[r][c] == 1:
            if r == 0 or r == rows - 1 or c == 0 or c == cols -1:
                dfs(r, c)

count = 0
for r in range(rows):
    for c in range(cols):
        if grid[r][c] == 1 and (r, c) not in visited:
            count += 1

return count
```

Pattern:

```
DFS purpose:
mark boundary-connected land

Returns:
nothing

Outer loop:
start from boundary land

Final count:
unvisited land cells
```

## Drill 7 - Count water cells touching island

```
1. What should dfs(r,c) represent?
Return the number of water cells touching the island

2. Should water return:
1

3. Should outside-grid return:
0

4. Should visited cell return:
0

5. What should one valid land cell contribute?
asking 4 neighbours
```

## Drill 8 - Surrounded Regions

Grid:

```
X X X X
X O O X
X X O X
X O X X
```

Rule:

```
Any O completely surrounded by X
should be changed to X.
```

Expected result:

```
X X X X
X X X X
X X X X
X O X X
```

Solution:

```
1. What should dfs represent?
dfs(r,c) = mark this O and all connected O cells as safe - connected to boundary

2. What should visited represent?
visited = O cells connected to boundary
unvisited = surrounded

3. Which O cells are unvisited after boundary DFS?
(1, 1), (1, 2), (2, 2)

4. Which cells get flipped?
(1, 1), (1, 2), (2, 2)
```

Code implementation:

```
def solve(board):
    rows = len(board)
    cols = len(board[0])
    visited = set()

    def dfs(r, c):
        if r < 0 or r >= rows or c < 0 or c >= cols:
            return
        if board[r][c] != 'O':
            return
        if (r, c) in visited:
            return

        visited.add((r, c))

        dfs(r+1, c)
        dfs(r-1, c)
        dfs(r, c+1)
        dfs(r, c-1)

    for r in range(rows):
        for c in range(cols):
            is_boundary = (
                r == 0 or
                r == rows - 1 or
                c == 0 or
                c == cols - 1
            )
            if board[r][c] == 'O' and is_boundary:
                dfs(r, c)

    for r in range(rows):
        for c in range(cols):
            if board[r][c] == 'O' and (r, c) not in visited:
                board[r][c] = "X"

    return board
```

## Big Checkpoint

### Family 1 — Explore one region

```
Number of Islands
Largest Area
Island Perimeter
Flood Fill
```

Start:

```
one cell
```

### Family 2 — Boundary-connected

```
Enclosed Land
Boundary-connected Cells
Surrounded regions
```

Start:

```
all boundary sources
```

### Family 3 — Reverse Reachability

```
Can reach ocean?
Can reach boundary?
Pacific Atlantic Water Flow
```

Start:

```
the target
```

and explore backwards.

**Single-source
**
Example:

```
Flood Fill
```

Start:

```
(sr, sc)
```

Question:

```
What can THIS cell reach?
```

**Multi-source**

Example:

```
Boundary-connected land
```

Start:

```
all boundary land cells
```

Question:

```
What can ANY of these cells reach?
```

```
Single-source:
one starting point

Multi-source:
a set of starting points
```
