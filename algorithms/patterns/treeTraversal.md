## Problem

You are given a binary tree:

```
        1
       / \
      2   3
     / \
    4   5
```

❓ Question

```
Return the maximum depth (height) of the tree.
```

## Solution

```
1. Pattern:
DFS (postorder traversal)

2. Why this pattern:
We need to compute the height of each subtree first, then use those results to compute the height of the current node.

3. Approach:
- Base case: if node is null → return 0
- Recursive step:
  height(node) = 1 + max(height(left), height(right))

4. Walkthrough: (bottom-up)
- Node 4 → height = 1
- Node 5 → height = 1
- Node 2 → 1 + max(1,1) = 2
- Node 3 → height = 1
- Node 1 → 1 + max(2,1) = 3
```

Answer: 3
