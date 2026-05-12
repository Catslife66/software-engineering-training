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
