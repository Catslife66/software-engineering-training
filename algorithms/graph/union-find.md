# Union-Find

Sometimes the problem changes, not the graph.

We still have a graph.

We still have nodes.

We still have weighted edges.

But now we're solving a completely different optimisation problem.

Imagine you're an engineer for a telecommunications company.

You have four towns.

```
      A

   B     C

      D
```

You need to lay fibre optic cable between them.

Each possible cable has a cost.

```
      A
    /   \
  3/     \4
  /       \
 B---2-----C
  \       /
 5 \     /1
    \   /
      D
```

The available cables are:

```
A-B = 3
A-C = 4
B-C = 2
B-D = 5
C-D = 1
```

After removing B-D and A-C, we keep:

```
A-B = 3
B-C = 2
C-D = 1
```

The network becomes:

```
A --3-- B --2-- C --1-- D
```

Every town is connected, and the total construction cost is:

```
3 + 2 + 1 = 6
```

That is much cheaper than building every cable for cost 15.

**Why this structure is called a tree**

The final network has:

- all four nodes connected;
- no cycle;
- exactly three edges.

If we removed any one of those three edges, the network would split.

So a spanning tree means:

- spanning: it includes every node;
- tree: it is connected and contains no cycles.

And minimum means its total edge cost is as small as possible.

Notice the strategy that appeared naturally:

Prefer cheap edges, but never add an unnecessary cycle.

That is the heart of Kruskal’s algorithm.

## Kruskal’s MST

Kruskal thinks globally:

1. Look at every edge in order from cheapest to most expensive.
2. Add the edge if it connects two parts that are not already connected.
3. Skip it if it would create a cycle.
4. Stop after selecting number_of_nodes - 1 edges.

For each edge from cheapest to most expensive:

```
If the two endpoints are in different connected groups:
    add the edge
    combine the groups

If they are already in the same group:
    skip the edge because it creates a cycle
```

This leads to the next engineering problem:

How can the algorithm efficiently tell whether two nodes are already in the same connected group?

So we need a structure that supports two operations:

1. Are these two nodes in the same group?
2. Combine their groups.

Those operations are called:

```
find
union
```

And the data structure is called **Union-Find**, also known as **Disjoint Set**.

Before syntax, let’s define the jobs:

```
find(node):
Which connected group does this node belong to?

union(a, b):
Merge the groups containing a and b.
```

So the decision rule is:

```
if find(node1) != find(node2):
    add edge
    union(node1, node2)
else:
    skip edge
```

Initially, every node is the representative of its own group:

```
parent = {
    "A": "A",
    "B": "B",
    "C": "C",
    "D": "D"
}
```

Meaning:

```
A belongs to group A
B belongs to group B
C belongs to group C
D belongs to group D
```

So at the beginning:

```
{A} {B} {C} {D}
```

The find(node) operation follows parent links until it reaches a node that points to itself.

For example:

```
parent["A"] == "A"
```

So:

```
find("A") == "A"
```

Now suppose Kruskal adds:

```
C-D = 1
```

We need to merge the groups containing C and D.

A simple union could make D point to C:

```
parent["D"] = "C"
```

Now:

```
parent:
A → A
B → B
C → C
D → C
```

This represents:

```
{A} {B} {C, D}
```

In a general Union-Find implementation, union should connect the roots:

```
root_a = find("A")
root_b = find("B")

parent[root_a] = root_b
```

The basic recursive find is:

```
def find(node):
    if parent[node] == node:
        return node

    return find(parent[node])
```

Its logic is:

```
Does this node point to itself?
├── Yes → it is the root
└── No  → continue searching through its parent
```
