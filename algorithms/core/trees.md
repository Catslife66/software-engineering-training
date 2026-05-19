# Trees

Trees are one of the most important structures in computer science.

They appear everywhere:

- file systems
- databases
- compilers
- DOM trees
- search engines
- AI models
- networking routing tables

And recursion becomes very natural when working with trees.

## What a tree look like

Example:

```
        A
       / \
      B   C
     / \   \
    D   E   F
```

Important terminology:

- Root → top node (A)
- Parent → node with children (A, B, C)
- Child → node below another node (B, C)
- Leaf → node with no children (D, E, F)
- Edge → connection between nodes

## Why Trees Are Perfect for Recursion

Look at node B:

```
      B
     / \
    D   E
```

Processing B naturally means:

```
process B
process left subtree
process right subtree
```

Which is naturally recursive.

## Memerise this

```
def f(node):
    if node is None:
        return base_value

    left = f(node.left)
    right = f(node.right)

    return combine(left, right, node)
```

## First Tree Algorithm Pattern

The most important starting point is **Tree Traversal**.

1️⃣ **Preorder**
Root → Left → Right

Used for:

- copying a tree
- serialization

```
def preorder(node):
    if node is None:
        return

    print(node.value)
    preorder(node.left)
    preorder(node.right)
```

2️⃣ **Inorder**
Left → Root → Right

Used for:

- binary search trees
- sorted order retrieval

```
def inorder(node):
    if node is None:
        return

    inorder(node.left)
    print(node.value)
    inorder(node.right)
```

3️⃣ **Postorder**
Left → Right → Root

Used for:

- deleting trees
- computing subtree values

```
def postorder(node):
    if node is None:
    return

    postorder(node.left)
    postorder(node.right)
    print(node.value)
```

Exxample:

```

        A
       / \
      B   C
     / \   \
    D   E   F

```

1. Preorder Traversal: A → B → D → E → C → F
2. Inorder Traversal: D → B → E → A → C → F
3. Postorder Traversal: D → E → B → F → C → A

## Quick intuition trick

Remember this:

| Traversal | Use case intuition    |
| --------- | --------------------- |
| Preorder  | build / copy tree     |
| Inorder   | sorted order (BST)    |
| Postorder | compute from children |

## One Beautiful Insight (important)

If the tree is a Binary Search Tree (BST):

```
left values < node < right values
```

Then:

```
Inorder traversal = sorted order
```

Example:

```
        5
       / \
      3   8
     / \   \
    1   4   9
```

Inorder traversal:

```
1 3 4 5 8 9
```

Sorted.

This property is extremely important in computer science.

## Height of a tree

height(node) = 1 + max(height(left), height(right))

This means:

> To compute the height of a node, we must know the heights of its children first.

**Python Implementation**

```
def height(node):

    if node is None:
        return 0

    left_height = height(node.left)
    right_height = height(node.right)

    return 1 + max(left_height, right_height)
```

Notice the order:

1️⃣ compute left subtree
2️⃣ compute right subtree
3️⃣ compute current node

That is postorder recursion.

### Important Algorithm Insight

Whenever a problem says something like: **compute something from children**

You should immediately think: Postorder traversal

Examples:

Postorder is used for:

- tree height
- subtree sums
- diameter of tree
- balanced tree checks
- deleting a tree
- expression evaluation
- lowest common ancestor variants

Because you need child results first.

## Maximum value

Return the maximum value in a binary tree

```
1. What does f(node) represent?
maximum value in the subtree rooted at this node

2. Base case?
if node is None:
    return -infinity

3. Recursive formula?
return max(node.val, f(node.left), f(node.right))
```

## Sum of Tree

Return the sum of all node values

**1. What does f(node) represent?**

```
Sum of all values in the subtree rooted at this node
```

**2. Base case?**

```
if node is null → return 0
```

**3. Recursive formula?**

```
return node.val + f(node.left) + f(node.right)
```

## Count Nodes

Return the number of nodes in a binary tree

**1. What does f(node) represent?**

```
f(node) = number of nodes in subtree rooted at node
```

**2. Base case?**

```
if node is null → return 0
```

**3. Recursive formula?**

```
return 1 + f(node.left) + f(node.right)
```

## Numbers of leaf nodes

Return the number of LEAF nodes in a binary tree

A leaf node means:

```
node.left is None AND node.right is None
```

```
1. What does f(node) represent?
number of leaf nodes in the subtree rooted at this node

2. Base case?
if node is None -> return 0

3. Recursive formula?
count = 1 if node.left is None and node.right is None else 0
return count + f(node.left) + f(node.right)

```

## Node contains target

Problem

Return True if a binary tree contains a value target

Example:

target = 7

Return:

True if any node has value 7

```
1. What does f(node, target) represent?
whether the subtree rooted at this node contains target

2. Base case?
if node is None → False
if node.val == target → True

3. Recursive formula?
return f(node.left, target) OR f(node.right, target)
```

## Minimum height of a tree

Problem

Return the minimum depth of a binary tree

Minimum depth:

```
shortest path from root to ANY leaf
```

```
1. What does f(node) represent?
minimum depth of a subtree rooted in this node

2. Base case?
if node is None:
    return 0

if node is leaf:
    return 1

3. Recursive formula?

if node.left is None:
    return 1 + f(node.right)

if node.right is None:
    return 1 + f(node.left)

return 1 + min(f(node.left), f(node.right))
```

**KEY IDEA**

For minimum depth:

```
ONLY real paths to leaves count
```

Missing child:

```
does NOT count as a valid path
```

## Same Tree (structure + values)

Given two binary trees, return True if they are identical - two trees have the **same structure** and the **same values**.

**1. What does f(node1, node2) represent?**

```
Whether the two subtrees rooted at node1 and node2 are identical
```

**2. Base cases?**

```
if node1 is None and node2 is None:
    return True

if node1 is None or node2 is None:
    return False

if node1.val != node2.val:
    return False
```

**3. Recursive formula?**

```
return f(node1.left, node2.left) and f(node1.right, node2.right)
```

## Path Sum I

Given a binary tree and a target sum,
return True if there exists a root-to-leaf path
where the sum equals target.

**1. What does f(node, target) represent?**

```
Is there a root-to-leaf path starting from this node
such that the path sum equals target?

Path Sum = "Can I reach a leaf where remaining target becomes 0?"
```

At each node:

```
remaining target = target - node.val
```

**2. Base cases**

```
- if node is null → return False
- if node is leaf → return target == node.val
```

**3. Recursive formula:**

```
return f(left, target - node.val) OR f(right, target - node.val)
```

Example:

```
        5
       / \
      4   8
     /   / \
    11  13  4
   /  \
  7    2
```

target = 22

Step by step:

```
Start: f(5, 22)

Go left: f(4, 17)

Go left: f(11, 13)

Go right: f(2, 2)

Now: leaf node AND target == node.val → True
```

## Path Sum II

Return ALL root-to-leaf paths where sum = target

**1. What does f(node, target, path) represent?**

```
Explore all root-to-leaf paths starting from this node,
while tracking the remaining target and the current path.
```

So path stores:

```
the nodes chosen so far on the current route
```

**2. Backtracking pattern**

```
add node to path
explore left and right
remove node from path
```

Example:

```
                5
              /   \
             4     8
            /     / \
           11    13  4
          /  \       / \
         7    2     5   1
```

target = 22

Step by step:

```
Step 1 — Start
node = 5
path = []
target = 22

Step 2 — Go left (4)
path = [5, 4]
target = 13

Step 3 — Go left (11)
path = [5, 4, 11]
target = 2


Step 4 — Go left (7)
path = [5, 4, 11, 7]
target = -5 ❌

👉 Leaf but not valid → stop

Backtrack
remove 7
path = [5, 4, 11]

Step 5 — Go right (2)
path = [5, 4, 11, 2]
target = 0 ✅

👉 Leaf + valid → save path

result = [[5, 4, 11, 2]]

Backtrack again
remove 2 → [5, 4, 11]
remove 11 → [5, 4]
remove 4 → [5]

Step 6 — Go right (8)
path = [5, 8]
target = 9

Step 7 — Go left (13)
path = [5, 8, 13]
target = -4 ❌

👉 invalid → backtrack

Step 8 — Go right (4)
path = [5, 8, 4]
target = 5

Step 9 — Go left (5)
path = [5, 8, 4, 5]
target = 0 ✅

👉 valid path

result = [
  [5, 4, 11, 2],
  [5, 8, 4, 5]
]

```

## Formular

| Type               | Example                |
| ------------------ | ---------------------- |
| Value recursion    | height, sum, count     |
| Decision recursion | path sum, same tree    |
| Path recursion     | path sum II            |
| Backtracking       | add → explore → remove |

| Problem Type     | Formula                   |
| ---------------- | ------------------------- |
| Sum              | `left + right + node.val` |
| Count            | `1 + left + right`        |
| Height           | `1 + max(left, right)`    |
| Exists?          | `left or right`           |
| Both must match? | `left and right`          |

## What TYPE of value does this recursion return?

**Case 1 — Sum problem**

```
f(node) returns a NUMBER
```

So when node is None:

```
must return a valid number
```

For sum: 0

because: 0 is neutral for addition

Example

```
return f(left) + f(right)
```

If one side is None:

```
0 + something
```

**Case 2 — Maximum problem**

```
f(node) returns a maximum value
```

For None:

```
return -infinity
```

because: None should never become maximum

**Case 3 — Boolean decision problem**

```
f(node) returns True/False
```

For None: Depends on meaning.

Example: Contains target

```
None subtree does NOT contain target
```

So: return False

Example: Same tree

```
both None means identical
```

So: return True

**Case 4 — Void recursion / traversal**

Sometimes recursion returns NOTHING.

Example:

```
collect paths into result
```

Then:

```
if node is None:
    return
```

because: function does not return a value

**Cheat sheet**

| Problem Type      | Return on None     |
| ----------------- | ------------------ |
| Sum               | `0`                |
| Count             | `0`                |
| Max               | `-∞`               |
| Min               | `+∞` (sometimes)   |
| Boolean exists    | `False`            |
| Boolean all-valid | `True` (sometimes) |
| Void traversal    | `return`           |

## Base Case & Recursive Step

**Base case**

```
STOP recursion
```

Base cases answer:

```
“When should recursion end immediately?”
```

Usually:

- null node
- leaf node
- found answer
- invalid state

**Recursive step**

```
CONTINUE recursion
```

Recursive steps answer:

```
“How do I reduce the problem into smaller subproblems?”
```

Usually:

```
left = f(node.left)
right = f(node.right)
```

then combine.

## Covered Patterns

**✅ Bottom-up recursion**

Examples:

- height
- count nodes
- max value
- leaf count
- sum leaf nodes

Pattern:

```
left result + right result + combine
```

**✅ Decision recursion**

Examples:

- contains target
- same tree
- symmetric tree
- all positive

Pattern:

```
OR / AND recursion
```

**✅ Top-down recursion**

Examples:

- path sum
- left leaf tracking
- path building

Pattern:

```
pass information downward
```

**✅ Backtracking basics**

Examples:

- Path Sum II
- collect all paths

Pattern:

```
choose → explore → undo
```

```
Path list collected → backtracking
Numeric value returned → bottom-up recursion
Numeric/string passed downward → top-down state passing
```
