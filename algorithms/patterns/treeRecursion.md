## Problem 1

You are given a binary tree.

Return True if the tree is symmetric (mirror of itself), otherwise False.

Eample:

```
        1
       / \
      2   2
     / \ / \
    3  4 4  3
```

→ True

## Solution

```
1. Pattern:
Tree recursion / DFS

2. Why:
Symmetry means the left and right subtrees must be mirror images, so we recursively compare opposite children.

3. Approach:
- If root is null, return True
- Create helper function is_mirror(left, right)
- In helper:
   - if both are null → True
   - if only one is null → False
   - if values are different → False
   - otherwise check:
       is_mirror(left.left, right.right)
       AND
       is_mirror(left.right, right.left)


4. Walkthrough:
- is_mirror(2, 2),
    check values equal → continue
    compare is_mirror(left.left, right.right) and is_mirror(left.right, right.left)
            is_mirror(3, 3)                       is_mirror(4, 4)

- is_mirror(3, 3)
    check values equal → continue
    compare is_mirror(None, None) and is_mirror(None, None)
    both return True → s_mirror(3, 3) is True

- is_mirror(4, 4)
    check values equal → continue
    compare is_mirror(None, None) and is_mirror(None, None)
    both return True → s_mirror(4, 4) is True

- Back to: is_mirror(2, 2)
    True AND True → True

- is_mirror(root.left, root.right) → True
```

## Code implementation

```
def is_symmetric(root):
    if root is None:
        return True

    def is_mirror(left, right):
        if left is None and right is None:
            return True

        if left is None or right is None:
            return False

        if left.val != right.val:
            return False

        return (
            is_mirror(left.left, right.right)
            and
            is_mirror(left.right, right.left)
        )

    return is_mirror(root.left, root.right)
```

Example:

```
    1
   / \
  2   2
   \   \
    3   3

```

Walkthrough:

```
step 1 - is_mirror(2, 2)
         compare (left.left, right.right) and (left.right, right,left)
         is_mirror(none, 3) & is_mirror(3, none)
step 2 - is_mirror(none, 3)
         left is none and right is not none - return False
         stops because False AND anything is False
```

```
        1
       / \
      2   3
       \
        5

        Return ALL root-to-leaf paths as strings

```

## Problem 2

Given a binary tree:

```
        1
       / \
      2   3
       \
        5
```

❓ Question

Return ALL root-to-leaf paths as strings

Expected output

```
["1->2->5", "1->3"]
```

Solution:

```
1. What does f(node, path) represent?
Explore all root-to-leaf paths starting from this node, while carrying the current path so far.

2. Base case?
If node is null → return

3. Recursive step?
Add current node value to path.
Then explore left and right children.

4. Where do we add to result?
When node is a leaf:
node.left is None and node.right is None
→ add the completed path to result
```

Code implementation:

```
def binary_tree_paths(root):
    result = []

    def dfs(node, path):
        if node is None:
            return

        if path == "":
            new_path = str(node.val)
        else:
            new_path = path + "->" + str(node.val)

        if node.left is None and node.right is None:
            result.append(new_path)
            return

        dfs(node.left, new_path)
        dfs(node.right, new_path)

    dfs(root, "")
    return result
```
