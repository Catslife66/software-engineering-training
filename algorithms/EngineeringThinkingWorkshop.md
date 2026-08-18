# Engineering Thinking Workshop

## Module 0 — Foundations ✅

Goal:

Understand the language.

### Lesson 1

Problem vs Algorithm

What is the problem asking?

What is the answer?

### Lesson 2

Information

Question:

What fact matters?

Examples:

- maximum
- sum
- count
- merged interval

### Lesson 3

State

Question:

How will I store that information?

Examples:

- best
- count
- sum
- newInterval

### Lesson 4

Invariant

Question:

What promise stays true during the algorithm?

This is the heart of the module.

### Lesson 5

Transitions

Question:

When should the state change?

## Module 1 — Prefix Invariants ✅

This is the easiest invariant family.

Pattern:

```
state

=

answer for everything examined so far
```

Problems:

- Running maximum
- Running minimum
- Running sum
- Running count
- First occurrence
- Last occurrence
- Any?
- All?

## Module 2 — Pointer State Design

Families:

### Read / Write

- Remove Element

- Move Zeroes

### Start / Read

- Merge Intervals

- Insert Interval

### Two Readers

- Merge Sorted Array

### Opposite Direction

- Reverse Array

- Container With Most Water

- Palindrome

### Fast / Slow

- Cycle Detection

- Middle Node

- Happy Number

## Module 3 — Window State

Sliding Window

New question:

What information belongs to the current window?

- Fixed Window
- Variable Window
- Window Invariants

## Module 4 — Stack State

New question:

What previous information is still useful?

- Daily Temperatures

- Next Greater Element

- Largest Rectangle

## Module 5 — Graph State

New question:

What information have I already visited?

- DFS

- BFS

- Shortest Path

## Module 6 — Dynamic Programming

New question:

What information should I remember so I never solve the same problem twice?

## Every lesson will follow ONE template

Every lesson begins with this table.

| Step | Question                            |
| ---- | ----------------------------------- |
| 1    | What is the final answer?           |
| 2    | What information do I need?         |
| 3    | What state stores that information? |
| 4    | What invariant should stay true?    |
| 5    | When does the state change?         |
| 6    | Translate into code                 |
