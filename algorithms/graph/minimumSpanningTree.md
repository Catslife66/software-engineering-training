# Minimum Spanning Tree (MST) --- Prim, Kruskal, and Union-Find

A **Minimum Spanning Tree (MST)** solves this problem:

> Connect all nodes in a connected, weighted, undirected graph using the
> minimum possible total edge weight, without cycles.

An MST has three important properties:

1.  It connects every node.
2.  It contains no cycles.
3.  For `V` nodes, it contains exactly `V - 1` edges.

**Example graph:**

```text
      A
    /   \
  3/     \4
  /       \
 B---2-----C
  \       /
5  \     /1
     \   /
       D
```

Edges:

```text
A-B = 3
A-C = 4
B-C = 2
B-D = 5
C-D = 1
```

One MST is:

```text
A --3-- B --2-- C --1-- D
```

Total cost:

```text
3 + 2 + 1 = 6
```

There are 4 nodes and 3 accepted edges:

```text
edges = nodes - 1
      = 4 - 1
      = 3
```

## MST vs Shortest Path

This distinction is essential.

### Shortest-path problem

Question:

> What is the cheapest route from one starting node to another node (or all other nodes)?

Examples:

- BFS for unweighted graphs
- Dijkstra for non-negative weighted graphs
- Bellman-Ford when negative edges may exist

### Minimum-spanning-tree problem

Question:

> What is the cheapest way to connect the entire network?

Examples:

- Prim
- Kruskal

### Important distinction

```
Dijkstra
→ builds cheapest paths from a starting node

Prim / Kruskal
→ build a minimum-cost network connecting all nodes
```

`total_cost` in an MST algorithm means:

> The sum of the weights of all accepted MST edges.

It does **not** mean the shortest-path cost from the starting node.

## Prim's Algorithm

Example:

```
      A
    /   \
  3/     \4
  /       \
 B---2-----C
  \       /
5  \     /1
     \   /
       D
```

Start from `A`.

**Initial state**

```
in_tree = {A}
```

Possible expanding edges:

```
A-B = 3
A-C = 4
```

Choose:

```
A-B = 3
```

State:

```
in_tree = {A, B}

mst_edges:
A-B = 3

total_cost = 3
```

**Next expansion**

Candidate edges:

```
A-C = 4
B-C = 2
B-D = 5
```

Choose:

```
B-C = 2
```

State:

```
in_tree = {A, B, C}

mst_edges:
A-B = 3
B-C = 2

total_cost = 5
```

**Next expansion**

`C-D = 1` is now available.

Choose:

```
C-D = 1
```

**Final state**:

```
in_tree = {A, B, C, D}

mst_edges:
A-B = 3
B-C = 2
C-D = 1

total_cost = 6
```

Concept:

```
Domain:
Minimum spanning tress

Problem:
Connect all nodes with minimum total edge cost, without creating cycles.

State:
in_tree
→ nodes already connected into our growing MST

mst_edges
→ edges we've accepted into the MST

total_cost
→ total weight of accepted MST edges

heap
→ candidate expanding edges

Data Structure:
Min-heap

Core Idea:
Grow one tree outward.

Invariant:
After every accepted edge, `mst_edges` remains one connected tree containing exactly the nodes in `in_tree`; no accepted edge creates a cycle.

Frontier:
All candidate edges that could expand the tree.

Greedy rule:
Choose the cheapest edge that expands the current tree to a new node.

Heap entry:
(edge_weight, from_node, to_node)

Priority:
cheapest candidate EDGE WEIGHT

Transition:
pop cheapest candidate edge
          ↓
destination already in tree?
      ↙ yes       ↘ no
    skip          accept edge
                      ↓
                 add new node
                      ↓
              explore its neighbours
                      ↓
             push expanding edges

Stale rule:
If the destination is already in_tree, skip it.

Termination:
When every reachable node has joined the tree.
```

Code Skeleton

```python
import heapq

def prim(graph, start):
    in_tree = set()
    mst_edges = []
    total_cost = 0

    # Dummy entry lets the start node enter through the normal loop.
    heap = [(0, None, start)]

    while heap:
        weight, from_node, to_node = heapq.heappop(heap)

        if to_node in in_tree:
            continue

        in_tree.add(to_node)

        # Do not record the dummy starting edge.
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

Skeleton memory

```
initialise tree state
        ↓
push dummy start entry
        ↓
while heap
        ↓
pop cheapest candidate edge
        ↓
destination already in tree?
    yes → skip
    no  → add destination
           ↓
         record real edge
           ↓
         add edge cost
           ↓
         push new expanding edges
```

## Prim vs Dijkstra

They can look similar because both use a min-heap, but their state means
different things.

```
  Dijkstra                           Prim
  ---------------------------------- ---------------------------------
  Shortest-path problem              MST problem
  Heap stores candidate paths        Heap stores candidate edges
  Priority = path cost               Priority = edge weight
  Tracks `best_cost[node]`           Tracks `in_tree`
  Finds cheapest routes from start   Builds one minimum-cost network
```

Dijkstra:

"What is the cheapest candidate PATH to explore next?"

Prim:

"What is the cheapest candidate EDGE that expands my tree?"

## Kruskal's Algorithm

Kruskal does **not** grow outward from a starting node.

Instead:

> Look at all edges globally, from cheapest to most expensive.

Initially every node is its own connected group:

```
{A}   {B}   {C}   {D}
```

Kruskal gradually merges groups:

```
many separate trees
        ↓
merge
        ↓
fewer larger trees
        ↓
merge
        ↓
one spanning tree
```

A collection of separate trees is called a **forest**.

**Example Walkthrough**

Original edges:

```
A-B = 3
A-C = 4
B-C = 2
B-D = 5
C-D = 1
```

Sort by ascending weight:

```
C-D = 1
B-C = 2
A-B = 3
A-C = 4
B-D = 5
```

Initial groups:

```
{A}   {B}   {C}   {D}
```

**Edge 1: C-D = 1**

Different groups:

```
{C}   {D}
```

Accept.

Groups become:

```
{A}   {B}   {C, D}
```

MST:

```
C-D = 1
```

**Edge 2: B-C = 2**

Different groups:

```
{B}   {C, D}
```

Accept.

Groups become:

```
{A}   {B, C, D}
```

MST:

```
C-D = 1
B-C = 2
```

**Edge 3: A-B = 3**

Different groups:

```
{A}   {B, C, D}
```

Accept.

Groups become:

```
{A, B, C, D}
```

MST:

```
C-D = 1
B-C = 2
A-B = 3
```

Total:

```
1 + 2 + 3 = 6
```

**Edge 4: A-C = 4**

A and C are already in the same connected group.

Skip.

Adding this edge would create a cycle.

## Union-Find

```
PURPOSE
Track connected groups.

parent[node]
→ parent link toward root

find(node)
→ return group's root

union(a, b)
→ merge two groups

size[root]
→ number of nodes in root's group

PATH COMPRESSION
→ flatten parent paths during find()

UNION BY SIZE
→ attach smaller tree under larger tree
```

### Why Kruskal Needs Union-Find

Kruskal's job

> Which edge should we consider next?

Answer:

```
Cheapest remaining edge.
```

Union-Find's job

> Are the two endpoints already connected?

If yes:

```
skip edge
```

because it would create a cycle.

If no:

```
accept edge
merge the two groups
```

When Kruskal considers:

```
u --weight-- v
```

it needs to answer:

> Are `u` and `v` already connected through previously accepted edges?

Union-Find maintains the connected groups efficiently.

The two fundamental operations are:

```
find(node)
union(a, b)
```

### `find(node)`

`find(node)` returns:

> The representative/root of the connected group containing `node`.

Initially:

```
{A}   {B}   {C}   {D}
```

so:

```
find(A) = A
find(B) = B
find(C) = C
find(D) = D
```

After C and D are merged, perhaps C becomes the root:

```
{C, D}

find(C) = C
find(D) = C
```

Therefore:

```
find(a) == find(b)
```

means:

> `a` and `b` are already in the same connected group.

And:

```
find(a) != find(b)
```

means:

> They belong to different connected groups.

### `union(a, b)`

`union(a, b)` means:

> Merge the entire groups containing `a` and `b`.

Example:

```
{A, B}     {C, D}
```

Calling:

```
union(B, C)
```

produces:

```
{A, B, C, D}
```

We are merging **groups**, not merely changing two individual nodes.

### `parent`

Union-Find represents groups using parent links.

Initially:

```
parent = {
    "A": "A",
    "B": "B",
    "C": "C",
    "D": "D"
}
```

A node is a root when:

```
parent[node] == node
```

For example:

```
B → A
A → A
```

means B belongs to the group whose root is A.

A longer parent structure might look like:

```
D → C → B → A
            ↑
           root
```

Then:

```
find(D) = A
find(C) = A
find(B) = A
find(A) = A
```

### `size`

`size[root]` represents:

> Number of nodes in the connected group whose representative is `root`.

Example:

```
{A, B}

size[A] = 2
```

and:

```
{C, D}

size[C] = 2
```

The meaningful size is associated with the current root.

## Union-Find Optimisations

### Union by Size

Suppose:

```
Group A: 5 nodes
Group C: 2 nodes
```

When merging them, attach the smaller tree beneath the larger tree.

```
small tree
    ↓
large tree
```

rather than:

```
large tree
    ↓
small tree
```

### Why?

To keep the parent structure shallow.

Fewer parent links make future `find()` operations faster.

### Rule

```python
if size[root_a] >= size[root_b]:
    parent[root_b] = root_a
    size[root_a] += size[root_b]
else:
    parent[root_a] = root_b
    size[root_b] += size[root_a]
```

> **Union by Size keeps trees shallow when merging groups.**

### Path Compression

Suppose:

```text
E → D → C → A
            ↑
           root
```

Calling:

```python
find("E")
```

must follow:

```text
E → D → C → A
```

With path compression, the nodes encountered during `find()` are updated to point directly to the root.

Afterwards:

```
E ───→ A
D ───→ A
C ───→ A
       ↑
      root
```

So:

```python
parent["E"] = "A"
parent["D"] = "A"
parent["C"] = "A"
```

Code Skeleton:

```python
def find(node):
    if parent[node] != node:
        parent[node] = find(parent[node])

    return parent[node]
```

The key line:

```
parent[node] = find(parent[node])
```

does two jobs:

1.  recursively discovers the root;
2.  changes `node` to point directly to that root.

> **Path Compression flattens trees while finding roots.**

### Union by Size vs Path Compression

Keep these separate:

```
UNION BY SIZE
When?
→ During union()

Purpose?
→ Attach smaller tree below larger tree.

Result?
→ Avoid unnecessarily deep trees.
```

```
PATH COMPRESSION
When?
→ During find()

Purpose?
→ Make encountered nodes point directly to root.

Result?
→ Flatten existing parent paths.
```

Together they make Union-Find very efficient.

## Kruskal with Union-Find

Concept:

```
DOMAIN:
Minimum Spanning Tree

PROBLEM:
Connect all nodes with minimum total edge weight, without cycles.

STATE:
parent
→ representing connected groups

size
→ size of each root's connected group

mst_edges
→ accepted MST edges

total_cost
→ total weight of accepted edges

DATA STRUCTURES:
Sorted edge list
Union-Find

CORE IDEA:
Consider edges globally from cheapest to most expensive.

GREEDY RULE:
Accept the cheapest edge connecting two different groups.

INVARIANT:
Accepted edges form a cycle-free forest that can still be extended into an MST.

CYCLE CHECK:
find(a) == find(b)
→ already connected
→ skip

find(a) != find(b)
→ different groups
→ accept + union
```

Code Skeleton

```python
def kruskal(nodes, edges):
    edges.sort(key=lambda edge: edge[2])

    parent = {
        node: node for node in nodes
    }

    size = {
        node: 1 for node in nodes
    }

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

        if size[root_a] >= size[root_b]:
            parent[root_b] = root_a
            size[root_a] += size[root_b]
        else:
            parent[root_a] = root_b
            size[root_b] += size[root_a]

    for u, v, weight in edges:
        if find(u) != find(v):
            mst_edges.append((u, v, weight))
            total_cost += weight
            union(u, v)

    return mst_edges, total_cost
```

Skeleton Memory:

Layer 1 --- Kruskal

```
sort all edges by weight
        ↓
scan edges
        ↓
find(u) != find(v)?
    ↓ yes
accept edge
add cost
union groups
```

Layer 2 --- Union-Find

```
parent
size
  ↓
find()
→ locate root
→ path compression

union()
→ locate both roots
→ merge groups
→ union by size
```

## Prim vs Kruskal

**The Main Comparison**

```
  -----------------------------------------------------------------------
  Prim                                Kruskal
  ----------------------------------- -----------------------------------
  Grows one tree                      Merges many separate trees

  Starts from a node                  Does not need a start node

  Looks at edges expanding the        Looks at all edges globally
  current tree

  Uses a min-heap                     Uses sorted edge list

  Tracks `in_tree`                    Tracks connected groups

  Avoids adding a node twice          Avoids connecting nodes already in
                                      same group

  Greedy choice: cheapest expanding   Greedy choice: cheapest safe global
  edge                                edge

  Main supporting structure: heap     Main supporting structure:
                                      Union-Find
  -----------------------------------------------------------------------
```

**Mental pictures**

Prim:

```
        tree
         ↓
    grow outward
         ↓
     larger tree
```

Kruskal:

```
{A} {B} {C} {D}
       ↓
   merge groups
       ↓
{A,B} {C,D}
       ↓
     merge
       ↓
{A,B,C,D}
```

## Bellman-Ford vs Kruskal

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

## Recognition Rules

If the problem says:

> Connect every node with minimum total cost.

Think:

```
Minimum Spanning Tree
→ Prim or Kruskal
```

If your mental approach is:

> Start somewhere and keep expanding the current tree with the cheapest edge.

Think:

```
Prim
```

If your mental approach is:

> Sort every edge globally and keep the cheapest ones that do not create cycles.

Think:

```
Kruskal
```

If you need to repeatedly answer:

> Are these two nodes already in the same connected group?

Think:

```
Union-Find
```

## Common Confusions

### Prim is not Dijkstra

Both use heaps, but:

```
Dijkstra heap priority
→ candidate PATH COST

Prim heap priority
→ candidate EDGE WEIGHT
```

Dijkstra asks:

> What is the cheapest route from the start?

Prim asks:

> What is the cheapest edge that expands my network?

### Kruskal does not use `visited`

Kruskal does not grow one tree by visiting neighbours.

Instead, it tracks **connected groups**.

That is why it uses:

```text
find()
union()
```

rather than:

```text
visited
```

## One-Page Recall Map

```
                 MINIMUM SPANNING TREE
                          │
              connect all nodes cheaply
                    without cycles
                          │
              ┌───────────┴───────────┐
              │                       │
             PRIM                  KRUSKAL
              │                       │
       grow ONE tree            merge MANY trees
              │                       │
        start from node          sort all edges
              │                       │
          min-heap                Union-Find
              │                       │
 cheapest expanding edge      cheapest safe edge
                                      │
                              find(u) != find(v)
                                      │
                                  accept edge
                                      │
                                  union(u, v)
                                      │
                           ┌──────────┴──────────┐
                           │                     │
                    Path Compression       Union by Size
                    flatten on find        shallow on merge
```

Prim grows one tree using the cheapest expanding edge. Kruskal builds a forest using the cheapest globally safe edge, while Union-Find tells Kruskal whether two nodes are already connected.

```
                         GRAPH PROBLEMS
                              │
          ┌───────────────────┼────────────────────┐
          │                   │                    │
      EXPLORATION         DEPENDENCIES         WEIGHTED GRAPH
          │                   │                    │
      DFS / BFS              Kahn          ┌───────┴────────┐
                                            │                │
                                      SHORTEST PATH          MST
                                            │                │
                                      Dijkstra          Prim
                                      Bellman-Ford      Kruskal
                                                            │
                                                       Union-Find
```
