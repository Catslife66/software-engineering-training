# Algorithms

## What is an algorithm really?

An **algorithm** is just a **clear sequence of steps** for solving a problem.

Example:
Problem: find the largest number in a list.

Algorithm:

1. Assume the first number is the largest.
2. Check each next number.
3. If a number is larger, update the largest.
4. Return the largest at the end.

## Big-O

Big-O describes **how the cost of an algorithm grows as the input gets bigger**.

- **time complexity** → how much work it does
- **space complexity** → how much extra memory it uses

**O(1) - Constant time**
The work does not grow with input size.
Example:

- Random access of an element in an array
- Inserting at the beginning of linkedList

**O(n) - Linear time**
Work grows in direct proportion to input size.
Example:

- Looping through elements in an array
- Searching through a linkedList

**O(n²) - Quadratic time**
Usually nested loops over the same input.
Example:

- Insertion sort
- Selection sort
- Bubblesort

**O(log n) - Logarithmic time**
Each step cuts the problem down significantly.
Example: binary search

**O(n log n) - Quasilinear**
Very common in efficient sorting algorithms.
Example: merge sort, quicksort average case, heapsort

**O(n!) factorial time**
Example: Travelling salesman

## problem-solving framework

Whenever you solve an algorithm problem, ask:

1. What is the input?
2. What is the output?
3. What are the constraints?
4. What is the brute-force solution?
5. Can I do better with a data structure or pattern?
6. What is the time complexity?
7. What is the space complexity?

When we say an algorithm **scales well**, we mean:

> Performance does not degrade too badly as the input becomes very large.

## Golden pattern

```
Hashing → duplicates / lookup
Two pointers → sorted arrays
Sliding window → subarrays / substrings
Stack → nested / matching
Recursion → break into smaller problems
Tree → combine left & right
BFS → shortest path
DFS → explore / islands
DP → reuse overlapping subproblems
```
