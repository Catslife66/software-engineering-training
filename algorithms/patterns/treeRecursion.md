## Problem

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
