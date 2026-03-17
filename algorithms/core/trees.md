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

## Quick intuition trick

Remember this:

| Traversal | Use case intuition    |
| --------- | --------------------- |
| Preorder  | build / copy tree     |
| Inorder   | sorted order (BST)    |
| Postorder | compute from children |
