# Minimum Spanning Tree

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

Problem

Find the cheapest set of edges that:

- connects every node
- contains no cycles

State

```
Union-Find
---------
parent
size

MST being built
---------------
mst_edges
total_cost
```

Suppose the input is

```
edges = [
    ("A", "B", 3),
    ("A", "C", 4),
    ("B", "C", 2),
    ("B", "D", 5),
    ("C", "D", 1)
]
```

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

||

For each edge (u, v, weight):

```
1. Check whether u and v are already in the same connected group.
2. If they are different:
    - add this edge to mst_edges
    - add weight to total_cost
    - union the two groups
3. If they are already connected:
    - skip the edge, because it would create a cycle
```

So the decision is driven by:

```
find(u) != find(v)
```

If true, we keep the edge.

If false, we reject it.

```
for u, v, weight in edges:
    if find(u) != find(v):
        mst_edges.append((u, v, weight))
        total_cost += weight
        union(u, v)
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

Build the entire algorithms

Step 1 — Sort

```
edges.sort(key=lambda edge: edge[2])
```

Step 2 — Initialise Union-Find

```
parent = ...
size = ...
```

Step 3 — Initialise MST state

```
mst_edges = []
total_cost = 0
```

Step 4 — Process edges

```
for u, v, weight in edges:

    if find(u) != find(v):

        mst_edges.append((u, v, weight))
        total_cost += weight
        union(u, v)
```

Step 5 — Return

```
return mst_edges, total_cost
```

Walkthrough:

```
Thinking:
Look at the entire graph.
Take the cheapest safe edge.

State:
parent
size
mst_edges
total_cost

Data Structure:
Sorted edge list
Union-Find

Greedy Rule:
Cheapest edge that connects two different groups.
```

Code skeleton:

Assume the graph is given as:

```
edges = [
    (u, v, weight),
    ...
]

def kruskal(nodes, edges):

    edges.sort(key=lambda edge: edge[2])

    parent = {node: node for node in nodes}
    size = {node: 1 for node in nodes}

    mst_edges = []
    total_cost = 0

    def find(node):
        if parent[node] != node:
            parent[node] = find(parent[node])
        return parent[node]

    def union(a, b):
        root_a = find(a)
        root_b = find(b)

        if root_a == root_b:
            return

        if size[root_a] < size[root_b]:
            parent[root_a] = root_b
            size[root_b] += size[root_a]
        else:
            parent[root_b] = root_a
            size[root_a] += size[root_b]

    for u, v, weight in edges:
        if find(u) != find(v):
            mst_edges.append((u, v, weight))
            total_cost += weight
            union(u, v)

    return mst_edges, total_cost

```

## Union-Find

### Path Compression

Without it, find("A") follows:

```
A → B → C
```

every time.

After finding the root, we can make A point directly to C:

```
A ─────→ C
B ─────→ C
C ─────→ C
```

The recursive version becomes:

```
def find(node):
    if parent[node] != node:
        parent[node] = find(parent[node])

    return parent[node]
```

The important line is:

```
parent[node] = find(parent[node])
```

It does two jobs:

- Finds the root.
- Rewrites the parent link to point directly to that root.

We start with:

```
A → B → C → C
```

which means:

```
parent = {
    "A": "B",
    "B": "C",
    "C": "C"
}
```

Now we call:

```
find("A")
```

Step 1

A is not the root.

So:

```
parent["A"] = find("B")
```

We don't know the answer yet, so we recurse.

Step 2

B is not the root.

So:

```
parent["B"] = find("C")
```

Recurse again.

Step 3

C is the root.

```
parent["C"] == "C"
```

Return:

```
"C"
```

Step 4 (Returning)

Now the recursion unwinds.

For B:

```
parent["B"] = "C"
```

(No change.)

Return "C".

For A:

```
parent["A"] = "C"
```

This does change.

Return "C".

Final parent table

```
parent = {
    "A": "C",
    "B": "C",
    "C": "C"
}
```

### Union by Size (or Union by Rank)

Imagine this sequence

Initially:

```
A   B   C   D   E
```

Each is its own parent.

Now suppose we perform:

```
union(A, B)
union(B, C)
union(C, D)
union(D, E)
```

One possible parent structure becomes:

```
A → B → C → D → E
                ↑
                |
              Root
```

Now imagine calling:

```
find("A")
```

How many parent links must it follow?

```
A → B → C → D → E
```

That's 4 hops.

Now imagine 1 million nodes.

```
A → B → C → D → ...
```

find(A) becomes very slow.

Even with path compression, the first call can still be expensive.

**Union by Size**

Stores:

```
How many nodes are in this tree?
```

For example:

```
      C
     / \
    B   D
   /
  A
```

Size:

```
size[C] = 4
```

**Union by Rank**

Stores:

```
An estimate of the height of the tree.
```

Initial State

Initially:

```
parent = {
    "A": "A",
    "B": "B",
    "C": "C",
    "D": "D"
}

size = {
    "A": 1,
    "B": 1,
    "C": 1,
    "D": 1
}
```

Each tree contains exactly one node.

Now suppose we union C and D.

Before the union:

```
C      D

size(C) = 1
size(D) = 1
```

We choose C as the new root.

So:

```
parent["D"] = "C"
```

the tree becomes:

```
    C
    |
    D
```

Now C's tree contains:
`C
D`
So:

```
size["C"] = 2
```

Suppose later we have two trees.

Tree 1

```
      C
     / \
    B   D
   /
  A
size["C"] = 4
```

Tree 2

```
E
size["E"] = 1
```

Now we want to perform:

```
union(C, E)
```

We have two choices.

Option 1

Attach the big tree underneath the small tree.

```
E
|
C
/|\
...
```

The tree becomes taller.

Option 2

Attach the small tree underneath the big tree.

```
      C
     /|\
    B D E
   /
  A
```

The height hardly changes.

Choose option 2 because find("E") only takes 1 recursive call takes E -> C

So the engineering principle is:

> Always attach the smaller tree underneath the larger tree.

That keeps the trees shallow, which makes future find() operations fast.

### The Union by Size algorithm

We already know:

```
root_a = find(a)
root_b = find(b)
```

Now we compare:

```
size[root_a]
size[root_b]
```

Suppose:

```
size[root_a] = 2
size[root_b] = 5
```

Which parent pointer should we update?

So we perform:

```
parent[root_a] = root_b
size[root_b] = 7
```

Without Union by Size

You might accidentally do:

```
parent[root_b] = root_a
```

which produces:

```
Small tree
    ▲
    │
Big tree
```

making the tree taller.

With Union by Size

You always do:

```
parent[smaller_root] = larger_root
```

which produces:

```
Large tree
    ▲
    │
Small tree
```

keeping the tree shallow.

Code skeleton:

```
if size[root_a] < size[root_b]:
    parent[root_a] = root_b
    size[root_b] += size[root_a]
else:
    parent[root_b] = root_a
    size[root_a] += size[root_b]
```

## Prim's algorithm

Look at the graph again.

Kruskal's thinking

Kruskal says:

> "I don't care where you start."

It looks at **every edge in the graph**.

```
C-D = 1
B-C = 2
A-B = 3
A-C = 4
B-D = 5
```

It simply asks:

```
"What's the cheapest edge in the whole graph?"
```

Prim's thinking

Prim says:

> Only choose an edge that expands the current tree by one new node.

Kruskal

```
Whole graph

✓ Cheapest edge anywhere
```

It thinks globally.

Prim

```
Current tree

A

Available edges:

A-B = 3
A-C = 4
```

It thinks locally.

We're standing at:

```
Tree = {A}
```

Available edges:

```
A-B = 3
A-C = 4
```

So:

```
A-B = 3
```

is the **cheapest expanding edge**, so we add it.

Our tree now becomes:

```
A ----- B
```

Next available edges:

```
A-C = 4
B-C = 2
B-D = 5
```

So:

```
B-C = 2
```

Our tree now becomes:

```
A --3-- B --2-- C
```

the current node set is:

```
{A, B, C}
```

Now the only expanding edges are:

```
B-D = 5
C-D = 1
```

So:

```
C-D = 1
```

and the tree becomes:

```
A --3-- B --2-- C --1-- D
```

Total cost:

```
3 + 2 + 1 = 6
```

In Prim, the heap priority is:

```
cost of the single edge that would connect a new node to the tree
```

For example:

```
(2, "B", "C")
```

means:

```
Edge B-C costs 2 and could expand our current tree.
```

Using our fixed graph, after starting at A, the heap initially contains:

```
[
    (3, "A", "B"),
    (4, "A", "C")
]
```

Prim pops:

```
(3, "A", "B")
```

and our tree becomes:

```
{A, B}
```

Now we explore B's neighbours, just like we explored neighbours in Dijkstra.

So we push:

```
(2, "B", "C")
(5, "B", "D")
```

The heap now contains:

```
(2, "B", "C")
(4, "A", "C")
(5, "B", "D")
```

The heap now contains two edges leading to C.

```
(2, "B", "C")
(4, "A", "C")
```

Dijkstra

The priority queue might contain:

```
(8, "C")
(5, "C")
```

Two entries for the same node. When we pop the old one, we ignore it.

Prim

Now we have:

```
(4, "A", "C")
(2, "B", "C")
```

Two candidate edges for the same node.

We will pop:

```
(2, "B", "C")
```

first because it's cheaper.

Then later we'll eventually pop:

```
(4, "A", "C")
```

we check:

```
if C in in_tree:
    continue
```

That gives us Prim’s main loop shape:

```
while heap:
    weight, from_node, to_node = heapq.heappop(heap)

    if to_node in in_tree:
        continue

    # accept this edge
```

The invariant is:

```
`in_tree` contains exactly the nodes already connected into the current MST.
```

Once C joins the tree, the new useful edge from C is:

```
(1, "C", "D")
```

At that point the heap contains candidates including:

```
(1, "C", "D")
(4, "A", "C")
(5, "B", "D")
```

The min-heap pops:

```
(1, "C", "D")
```

So D joins the tree.

Now all four nodes are in:

```
{A, B, C, D}
```

and the chosen MST edges are:

```
A-B = 3
B-C = 2
C-D = 1
```

Total cost:

```
6
```

Prim has produced the same MST as Kruskal, but by a different strategy:

- Kruskal: cheapest safe edge anywhere.
- Prim: cheapest edge that expands the current tree.

Walkthrough:

```
Problem:
Connect every node with minimum total edge cost.

State:
in_tree
mst_edges
total_cost
heap

Invariant:
in_tree is always one connected tree, and mst_edges never contains a cycle.

Frontier:
All candidate edges that could expand the tree.

Greedy rule:
Take the cheapest expanding edge.

Stale rule:
If the destination is already in_tree, skip it.

Termination:
When every reachable node has joined the tree.
```

Code skeleton:

Assume `graph` is an adjacency list like:

```
graph = {
    "A": [("B", 3), ("C", 4)],
    "B": [("A", 3), ("C", 2), ("D", 5)],
    "C": [("A", 4), ("B", 2), ("D", 1)],
    "D": [("B", 5), ("C", 1)]
}

import heapq

def prim(graph, start):
    in_tree = set()
    mst_edges = []
    total_cost = 0

    heap = [(0, None, start)]

    while heap:
        weight, from_node, to_node = heapq.heappop(heap)

        if to_node in in_tree:
            continue

        in_tree.add(to_node)

        if from_node is not None:
            mst_edges.append((from_node, to_node, weight))
            total_cost += weight

        for neighbour_node, edge_weight in graph[to_node]:
            if neighbour_node not in in_tree:
                heapq.heappush(
                    heap,
                    (edge_weight, to_node, neighbour_node)
                )

    return mst_edges, total_cost

```

## Summary

State

```
parent
size
```

`find(node)`

Responsibility

```
Find the representative (root) of the node's connected group.
```

Path Compression

Responsibility

```
After finding the root, make every visited node point directly to it.
```

This speeds up future find() calls.

`union(a, b)`

Responsibility

```
Merge two different connected groups.
```

Compare Bellman-Ford and Kruskal

| Bellman-Ford                      | Kruskal                                            |
| --------------------------------- | -------------------------------------------------- |
| Goal: cheapest **path**           | Goal: cheapest **network**                         |
| State: `best_cost`                | State: `parent`, `size`, `mst_edges`, `total_cost` |
| Update: relax an edge             | Update: add an edge if it doesn't create a cycle   |
| Data structure: edge list         | Data structure: sorted edge list + Union-Find      |
| Invariant: shortest costs improve | Invariant: selected edges never contain a cycle    |

Both algorithms scan an edge list, but they ask completely different questions.

Bellman-Ford asks:

> "Can this edge improve the shortest path?"

Kruskal asks:

> "Can this edge connect two different groups without creating a cycle?"

Same graph.

Same edge list.

Completely different optimisation problem.

**Prim**

What does Prim repeatedly do?

```
1. Pop the cheapest edge from the heap.
2. Explore one node's neighbours.
3. Push new candidate edges into the heap.
```

**Kruskal**

What does Kruskal repeatedly do?

```
1. Sort all edges once.
2. Scan every edge once.
3. Use Union-Find to decide whether to keep it.
```
