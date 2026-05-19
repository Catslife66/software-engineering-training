## Problem 1 - Tree Structure

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

**Solution**

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

**Code implementation**

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

## Problem 2 - Return ALL Root-to-Leaf Path as Strings

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

**Solution**:

```
1. What does f(node, path) represent?
Explore all root-to-leaf paths starting from node,
while path stores current route from root to this node.

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

**Code implementation**:

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

## Problem 3 - Return whether a root-to-leaf path forms a string target exists

Question:

```
Given a binary tree,
return True if there exists a root-to-leaf path
where the path values form the string target.
```

Example:

```
target = "124"
```

Path:

```
1 → 2 → 4
```

Solution:

```
1. What does f(node, path) represent?
whether there is a root-to-leaf path starting from this node where the path string so far can become target.

2. Base cases?
if node is None:
    return False

new_path = path + str(node.val)

if node is a leaf:
    return new_path == target

3. Recursive formula?
return f(node.left, new_path) OR f(node.right, new_path)
```

## Problem 4 - Sum leaf nodes

Return the sum of all LEAF nodes

```
1. What does f(node) represent?
   sum of leaf node values in subtree rooted at this node

2. Base case?

if node is None -> return 0

if node.left is None and node.right is None:
return node.val

3. Recursive formula?

return f(node.left) + f(node.right)
```

## Problem 5 - Count nodes

Return the number of nodes that have EXACTLY ONE child

Example:

```
    1
   / \
  2   3
 /
4
```

Nodes with exactly one child: 2

Answer: 1

```
1. f(node):
number of nodes with exactly one child in subtree rooted at node

2. Base case:
if node is None:
    return 0

3. Recursive formula:
current = 1 if exactly one child else 0

    (node.left is not None and node.right is None)
    or
    (node.left is None and node.right is not None)
a cleaner way: (node.left is None) != (node.right is None)

return current + f(node.left) + f(node.right)
```

## Problem 6 - count nodes

Return the number of EVEN-valued nodes in a binary tree

Example:

```
      5
     / \
    2   8
   /
  4
```

Even-valued nodes:

```
2, 8, 4
```

Answer: 3

```
1. What does f(node) represent?
numbers of even-valued nodes in subtree rooted at this node

2. Base case?
if node is None -> return 0

3. Recursive formula?

count = 1 if node.val % 2 == 0 else 0
return count + f(node.left) + f(node.right)
```

## Problem 7 - count nodes

Return the number of nodes GREATER than target

Example:

target = 5

```
1. What does f(node, target) represent?
number of nodes with value greater than target in subtree rooted at this node

2. Base case?
if node is None -> return 0

3. Recursive formula?
count = 1 if node.val > target else 0
return count + f(node.left, target) + f(node.right, target)
```

## Problem 8 - Return wethere all node values are positive

Return True if ALL node values are positive

```
1. What does f(node) represent?
whether all values in the subtree rooted at this node are positive

2. Base case?
if node is None:
    return True

if node.val <= 0:
    return False

3. Recursive formula?
return f(node.left) and f(node.right)
```

## Problem 9 - Sum LEFT leaf nodes

Return the SUM of all LEFT leaf nodes

A left leaf means:

```
- it is a leaf
- AND it is the left child of its parent
```

Example:

```
      3
     / \
    9   20
       /  \
      15   7
```

Left leaves:

```
9 and 15
```

Answer: 24

```
1. What does f(node, isLeft) represent?
sum the values in the left leaf in subtree rooted at this node

2. Base cases?
if node is None -> return 0

3. Recursive formula?
count = node.val if (
    node.left is None
    and node.right is None
    and isLeft
) else 0

return (
    count
    + f(node.left, True)
    + f(node.right, False)
)
```

## Problem 10 - Top-down and bottom-up question

### drill 1

Return the sum of every root-to-leaf number.

Each root-to-leaf path forms a number.

Example:

```
    1
   / \
  2   3
```

Path:

```
1→2 = 12
1→3 = 13
```

Answer: 25

Solution:

```
1. What does f(node, currentNumber) represent?
Explore all root-to-leaf paths while currentNumber stores the number formed from root to this node.

2. Base case?
if node is None:
    return 0

3. Prepare state?
newNumber = currentNumber * 10 + node.val

4. Leaf case?
if node is leaf:
    return newNumber

3. Recursive formula?
return (f(node.left, newNumber) + f(node.right, newNumber))

4. Top-down or bottom-up?
BOTH. Because we pass currenNumber downward and return sums upward.
```

### drill 2

Return ALL root-to-leaf paths whose sum is EVEN.

Example:

```
        1
       / \
      2   3
     /
    5
```

Paths:

```
1→2→5 = 8 (even)
1→3 = 4 (even)
```

Answer: [[1,2,5], [1,3]]

```
1. What does f(node, currentSum, path) represent?
explore all root-to-leaf paths starting from node, while currentSum stores sum value from root to this node and path stores current route from root to this node.

2. Base case?
if node is None:
    return

3. Prepare state?
path.append(node.val)
newSum = currentSum + node.val

4. Leaf condition?
if node is leaf:
    if newSum % 2 == 0:
        result.append(path.copy())
    path.pop()
    return

5. Recursive formula?
f(node.left, newSum, path)
f(node.right, newSum, path)

6. Cleanup?
path.pop()

7. Top-down, bottom-up, or backtracking?
top-down + backtracking

8. Why IS backtracking needed here?
because path is a shared mutable state
```

### drill 3

Return True if there exists a root-to-leaf path whose sum is even.

Example:

```
        1
       / \
      2   3
     /
    5
```

Solution:

```
1. What does f(node, currentSum) represent?
Determine whether there exists a root-to-leaf path
starting from this node whose final path sum is even,
while currentSum stores the sum from root to this node.

2. Base case?
if node is None:
    return False

3. Prepare state?
newSum = currentSum + node.val

4. Leaf condition?
if node is leaf:
    if newSum % 2 == 0:
        return True
    else:
        return False

5. Recursive formula?
return f(node.left, newSum) OR f(node.right, newSum)
```

### drill 4

Return ALL root-to-leaf paths whose length is ODD.

Example:

```
        1
       / \
      2   3
     /
    4

[1,2,4] length = 3 odd ✅
[1,3]   length = 2 even ❌
```

Answer: [[1,2,4]]

Solution:

```
1. What does f(node, path) represent?
explore all root-to-leaf paths with path length is odd starting from node, while path stores current route from root to this node.

2. Base case?
if node is None:
    return

3. Prepare state?
path.append(node.val)

4. Leaf condition?
if node is leaf:
    if len(path) % 2 != 0:
        result.append(path.copy())
    path.pop()
    return

5. Recursive formula?
f(node.left, path)
f(node.right, path)

6. Cleanup?
path.pop()

7. What recursion style(s) are involved?
top-down path-building + backtracking

8. Why is backtracking needed?
because path is a shared mutable state.
```
