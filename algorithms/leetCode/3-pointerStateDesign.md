# Pointer State Design

**Purpose**

In Module 1, the processed prefix had one simple answer state:

```
best
count
result
exists
```

Now we move to problems where the array itself contains different regions of meaning.

That is where pointers become useful.

The key rule for this module is:

> A pointer is not the idea. A pointer marks a boundary between regions of state.

So we will not begin with:

“Which pointer moves?”

We will begin with:

“What regions exist, and what does each region mean?”

## Read / Write

```
read ---> scans information
write --> records the answer
```

Examples:

- Remove Element
- Remove Duplicates
- Move Zeroes
- Remove Duplicates II

Imagine an array containing some values we want to keep and some we want to remove.

Example:

```
[3, 2, 2, 3]
```

Problem:

```
Remove every 3 in place.
```

The final useful values should be:

```
[2, 2]
```

Ignore code for now.

Ask:

While scanning the array, what kinds of regions might naturally appear?

Suppose we have processed part of it:

```
[2, ? | 2, 3]
```

There are really three kinds of information:

```
| kept values | disposable processed space | unread values |
```

This is the foundation of Read / Write.

```
Information: What information must survive?
All non-3 values

Accepted Region: What should nums[0:write] contain?
`nums[0:write]` contains exactly the accepted values from the processed prefix, in their original order.

Disposable region:
`nums[write:read]` is disposable because every accepted value discovered so far has already been preserved in `nums[0:write]`

Unread region:
`nums[read:]` has not been processed yet.

Invariant:
Before each read, `nums[0:write]` contains exactly the accepted values from the processed prefix.

Transition:
accepted value:
    preserve it
    accepted region grows
    read moves
    write moves

rejected value:
    preserve nothing
    read moves
    write stays
```

Visually:

```
| accepted | disposable | unread |
0         write        read
```

---

**Why two pointers appear**

We need one boundary to answer:

```
Where is the next unread value?
```

That becomes:

```
read
```

And another boundary to answer:

```
Where should the next kept value be placed?
```

That becomes:

```
write
```

So their meanings are:

```
read = current input position being examined.

write = next position where an accepted value belongs.
```

Notice that these meanings come from the regions. We did not invent the pointers first.

### Drill - Remove Duplicates from Sorted Array

Given a sorted array, keep only one copy of each value.

Example:

```
[1, 1, 2, 2, 3]
```

Useful result:

```
[1, 2, 3]
```

```
Information: What values must survive?
All unique values must survive

Accepted region: What should nums[0:write] contain?
nums[0:write] contains all unique values in the processed prefix

Comparison: What should the current nums[read] be compared with to decide whether it is a duplicate?
nums[read] == nums[write - 1]

Transition: When should write move? When should only read move?
if nums[read] == nums[write - 1]:
    read moves
    write stays

otherwise:
    copy nums[read] to nums[write]
    read moves
    write moves

Invariant:
Before examining nums[read], nums[0:write] contains all unique values from the processed prefix, in original order.
```

### Drill - Move Zeros

Problem:

Move all zeroes to the end while keeping the relative order of non-zero values.

Example:

```
[0, 1, 0, 3, 12]
```

Useful front part becomes:

```
[1, 3, 12, ...]
```

```
Information: What values must survive, and in what order?
All non-zero values must survive while keeping their relative order

Accepted region: What should nums[0:write] contain?
nums[0:write] contains exactly all non-zero values from the processed prefix, in their original order.

Disposable region: Why can nums[write:read] be overwritten?
nums[write:read] is disposable because all the accepted values have been moved to nums[0:write]

Transition: What happens when nums[read] is non-zero? What happens when it is zero?
if nums[read] != 0:
    copy nums[read] to nums[write]
    read moves
    write moves
otherwise:
    read moves
    write stays

Invariant:
Before examining nums[read], nums[0:write] contains all non-zero values from the processed prefix, in their original order.
```

### Drill - Remove Duplicates II

Problem:

Given a sorted array, allow each value to appear at most twice.

Example:

```
[1, 1, 1, 2, 2, 3]
```

Useful front part should become:

```
[1, 1, 2, 2, 3]
```

```
Information: What values must survive?
each value that appears at most twice must survive

Accepted region: What should nums[0:write] contain?
nums[0:write] contains the processed prefix with each distinct value kept at most twice, in original order

Comparison: Which already-preserved value should nums[read] be compared with to decide whether keeping it would create a third duplicate?
nums[read] == nums[write - 2]

Transition: When should write move? When should only read move?
if nums[read] == nums[write - 2]:
    reject current
    read moves
    write stays

otherwise:
    preserve current
    read moves
    write moves

Invariant:
Before examining nums[read], nums[0:write] contains the processed prefix with each distinct value kept at most twice, in original order.
```

### Summary

Three Read/Write problems we've rebuilt:

```
Remove Element
accept if current != target

Remove Duplicates
accept if current != last accepted value

Move Zeroes
accept if current != 0
```

The pointer mechanics are almost identical. What changes is only:

**What condition makes the current value worth preserving?**

The common invariant is:

nums[0:write] contains exactly the values from the processed prefix that satisfy the preservation rule.

And the transition rule is:

```
accepted → copy and grow write;
rejected → only advance read.
```

## Start / Read

The key question becomes:

> Where did the current group start, and how far does it extend?

That is why the two positions have different meanings:

```
start = beginning of the current group
read  = current element being examined
```

We are **tracking a group or range**.

---

**Summary Ranges**

Problem:

[0, 1, 2, 4, 5, 7]

Return:

["0->2", "4->5", "7"]

The groups are:

```
0,1,2
4,5
7
```

The important question is:

How do we know whether the current range continues or ends?

Suppose we are here:

```
[0, 1, 2, 4, 5, 7]
 ↑        ↑
start    read
```

If the current value continues the sequence, start stays where it is and read moves.

If the sequence breaks, the range from start to the previous element is finished.

So the mental model is:

```
| completed ranges | current range being tracked | unread |
```

The current range is not yet finished because we do not know where it ends until we encounter a break.

```
Information: What information must we preserve while scanning?
We need to preserve the start of the current consecutive range and determine where that range ends.

Start: What does start mean?
start marks the first element of the current consecutive range.

Read: What does read mean?
the current element we are examining to see whether the range continues.

Invariant: Complete:
Before examining nums[read], the elements from start through read - 1 form one consecutive range.

Transition: What event tells us the current range has ended?
if nums[read] != nums[read - 1] + 1:
    current completed range = nums[start] ... nums[read - 1]
otherwise:
    the range continues
    start stays
    read moves
```

Visually:

```
| completed ranges | confirmed current range | candidate/unread |
                   start                read
```

**The important Start / Read pattern**

During the scan:

```
start
↓
[ current confirmed range ] [ candidate ]
                             ↑
                            read
```

If the candidate continues the group:

```
start stays
read moves
```

If it breaks the group:

```
finish start ... read - 1
start = read
read moves
```

And after the loop:

Finish the last group.

That **final cleanup step** is common in grouping problems because a group often finishes either when we see a boundary or when input ends.

### Drill - Merge Intervals
