# Tree Path & Backtracking

| Skill                        | Focus              |
| ---------------------------- | ------------------ |
| Build path downward          | top-down recursion |
| Return all paths             | collect results    |
| Track path correctly         | mutable state      |
| Undo choices                 | backtracking       |
| Distinguish leaf vs non-leaf | path completion    |

## Drill 1 - Collect ALL Valid Paths

Problem

```
Return ALL root-to-leaf paths where:

sum(path) == target


                5
              /   \
             4     8
            /     / \
           11    13  4
          /  \       / \
         7    2     5   1


target = 22

Example result:

[[5,4,11,2], [5,8,4,5]]
```

**Solution:**

```
1. What does f(node, remaining, path) represent?
Explore all root-to-leaf paths starting from node,
while tracking current path and remaining target.

2. Base case:
if node is None:
    return

3. Choose:
path.append(node.val)

4. If leaf:
    if node.val == remaining:
        result.append(path.copy())
    path.pop()
    return

5. Explore:
f(node.left, remaining - node.val, path)
f(node.right, remaining - node.val, path)

6. Undo:
path.pop()
```

**Code structure:**

```
def dfs(node, remaining, path):
    if node is None:
        return

    path.append(node.val)

    if node.left is None and node.right is None:
        if node.val == remaining:
            result.append(path.copy())

        path.pop()
        return

    dfs(node.left, remaining - node.val, path)
    dfs(node.right, remaining - node.val, path)

    path.pop()
```

Walkthrough:

```
left side

enter dfs(5,22,[])
append 5 → [5]

enter dfs(4,17,[5])
append 4 → [5,4]

enter dfs(11,13,[5,4])
append 11 → [5,4,11]

enter dfs(7,2,[5,4,11])
append 7 → [5,4,11,7]
leaf, 7 != 2 → not valid
pop 7 → [5,4,11]
return to 11

enter dfs(2,2,[5,4,11])
append 2 → [5,4,11,2]
leaf, 2 == 2 → append copy to result
pop 2 → [5,4,11]
return to 11

done with both children of 11
pop 11 → [5,4]
return to 4

done with children of 4
pop 4 → [5]
return to 5

right side
enter dfs(8,17,[5])
append 8 → [5,8]

enter dfs(13,9,[5,8])
append 13 → [5,8,13]
leaf, 13 != 9 → not valid
pop 13 → [5,8]
return to 8

enter dfs(4,9,[5,8])
append 4 → [5,8,4]

enter dfs(5,5,[5,8,4])
append 5 → [5,8,4,5]
leaf, 5 == 5 → append copy to result
pop 5 → [5,8,4]
return to 4

enter dfs(1,5,[5,8,4])
append 1 → [5,8,4,1]
leaf, 1 != 5 → not valid
pop 1 → [5,8,4]
return to 4

done with both children of 4
pop 4 → [5,8]
return to 8

done with both children of 8
pop 8 → [5]
return to 5

done with both children of 5
pop 5 → []
finished
```

**Call stack visualisation:**

```
- f(5)
    append 5 -> [5]
    - f(4)
        append 4 -> [5, 4]
        - f(11)
            append 11 -> [5, 4, 11]
            - f(7)
                append 7 -> [5, 4, 11, 7], leaf != 2 → not valid
                finished, pop -> [5, 4, 11]
            - f(2)
                append 2 -> [5, 4, 11, 2] valid, add to result
                finished, pop -> [5, 4, 11]
                exit
            finished, pop -> [5, 4]
        finished, pop -> [5]
    - f(8)
        append 8 -> [5, 8]
        - f(13)
            append 13 -> [5, 8, 13], leaf != 9 → not valid
            finished, pop -> [5, 8]
        - f(4)
            append 4 -> [5, 8, 4]
            - f(5)
                append 5 -> [5, 8, 4, 5] valid, add to result
                finished, pop -> [5, 8, 4]
                exit
            - f(1)
                append 1 -> [5, 8, 4, 1], leaf != 5 → not valid
                finished, pop -> [5, 8, 4]
            finished, pop -> [5, 8]
        finished, pop -> [5]
    finished, pop -> []
```

## Drill 2 - Build ALL Root-to-Leaf Paths

Problem

Return ALL root-to-leaf paths.

Example:

```
        1
       / \
      2   3
       \
        5
```

Expected result:

```
[
  [1,2,5],
  [1,3]
]
```

Solution:

```
1. What does f(node, path) represent?
Explore all root-to-leaf paths starting from this node, while path stores the current route from root to this node.

2. Base cases?
if node is None:
    return

3. Recursive formula:
append node.val
if node is leaf:
    result.append(path.copy())
else:
    f(node.left, path)
    f(node.right, path)

path.pop()
```

## Drill 3 - Count Root-to-Leaf Paths

Problem

Return the number of root-to-leaf paths.

Example:

```
        1
       / \
      2   3
       \
        5
```

Root-to-leaf paths:

```
1 → 2 → 5
1 → 3
```

Answer: 2

Solution:

```
1. What does f(node) represent?
number of root-to-leaf paths in the subtree rooted at this node

2. Base cases:
if node is None:
    return 0

3. Recursive formula:
count = 1 if node is leaf else 0
return count + f(node.left) + f(node.right)

4. Why does a leaf return 1?
Because we reach the bottom of this path then we count 1.
```

## Drill 4 - Return ALL Root-to-Leaf Path Sums

Problem

Return ALL root-to-leaf path sums.

Example:

```
        1
       / \
      2   3
     /
    4
```

Paths:

```
1 → 2 → 4 = 7
1 → 3 = 4
```

Return: [7, 4]

Solution:

```
1. What does f(node, currentSum) represent?
explores all root-to-leaf paths while currentSum stores the path sum from root to this node.

2. Base cases?
if node is None:
    return

if node is leaf:
    result.append(currentSum + node.val)
    return

3. Recursive formula?
f(node.left, currentSum + node.val)
f(node.right, currentSum + node.val)
```

## Drill 5 - Return ALL Root-to-Leaf Path as Strings

Example:

```
        1
       / \
      2   3
       \
        5
```

Answer:

```
[
  "1->2->5",
  "1->3"
]
```

Solution:

```
1. What does f(node, path) represent?
Explore all root-to-leaf paths starting from node, where path is the string built so far from the root to the parent/current node.

2. Base cases?
if node is None:
    return

if node is leaf:
    result.append(new_path)
    return

3. Prepare state
if path == "":
    new_path = str(node.val)
else:
    new_path = path + "->" + str(node.val)

4. Recursive formula?
f(node.left, new_path)
f(node.right, new_path)
```

## Drill 6 - Return ALL Root-to-Leaf Path as Lists

Same example, expected output: [[1,2,5], [1,3]]

```
1. What does f(node, path) represent?
Explore all root-to-leaf paths starting from node,
while path stores the current route from root to this node.

2. Base cases?
if node is None:
    return

path.append(node.val)

if node is leaf:
    result.append(path.copy())
    path.pop()
    return

3. Recursive formula?
f(node.left, path)
f(node.right, path)
path.pop()

```

## Drill 7 - Path Filtering

Problem

Return ALL root-to-leaf paths where: path length >= k

Example:

```
        1
       / \
      2   3
     /
    4
```

if k = 3, then valid path is [1,2,4]

Solution:

```
1. f(node, path):

Explore all root-to-leaf paths starting from node,
while path stores the current route from root to this node.

2. Base case:
if node is None:
    return

3. Prepare state:
path.append(node.val)

4. Leaf case:
if node is leaf:
    if len(path) >= k:
        result.append(path.copy())
    path.pop()
    return

5. Recursive step:
f(node.left, path)
f(node.right, path)

6. Cleanup:
path.pop()
```

## Drill 8 — Return all root-to-leaf paths whose last value is even

Example:

```
        1
       / \
      2   3
     / \
    4   5
```

Root-to-leaf paths:

```
[1,2,4]  valid, leaf 4 is even
[1,2,5]  invalid, leaf 5 is odd
[1,3]    invalid, leaf 3 is odd
```

Answer: [[1,2,4]]

Solution:

```
1. What does f(node, path) represent?
explore all root-to-leaf paths starting from node,
while path stores current route from root to this node.

2. Base case?
if node is None:
    return

3. Prepare state?
path.append(node.val)

4. Leaf condition?
if node is leaf:
    if node.val % 2 == 0:
        result.append(path.copy())
    path.pop()
    return

5. Recursive step?
f(node.left, path)
f(node.right, path)

6. Cleanup?
path.pop()
```

## Drill 9 - Count valid root-to-leaf paths

Instead of returning the paths, return the number of root-to-leaf paths whose leaf value is even.

Example:

```
        1
       / \
      2   3
     / \
    4   5
```

Valid path: [1,2,4]

Answer: 1

Solution:

```
1. What does f(node) represent?
numbers of valid root-to-leaf paths rooted from node

2. Base case?
if node is None:
    return 0

3. Leaf condition?
if node is leaf:
    if node.val % 2 == 0:
        return 1
    else:
        return 0

4. Recursive formula?
return f(node.left) + f(node.right)
```
