# Interval Thinking

A pointeris simply a variable with a responsibility.

Every state variable has ONE responsibility.

## Pointer Pattern 1 — Read / Write

```
read ---> scans information
write --> records the answer
```

Examples:

- Move Zeroes
- Remove Duplicates
- Merge Strings
- Compress String
- Remove Element

The questions are always:

What am I reading?

What am I writing?

Walkthrough - 283.Move Zeros:

```
State Variable 1:
Name: read
Responsibility: Visit every element exactly once.
Moves when: Every iteration.
Stops when: End of array.

State Variable 2:
Name: write
Responsibility: The position where the next non-zero should be written.
Moves when: A non-zero has just been written.
Stops when: There are no more elements to read.

Phase 1: compress non-zero values left
write = 0

for read in range(len(nums)):
    if nums[read] != 0:
        nums[write] = nums[read]
        write += 1

Phase 2: fill the rest with zeroes
while write < len(nums):
    nums[write] = 0
    write += 1
```

## Pointer Pattern 2 — Start / Read

```
start -------------->
           read ---->
```

The distance between them is the current group.

Examples:

- Summary Ranges
- Group Consecutive Numbers
- Partition Labels
- Split Intervals

Questions become:

Where did this group begin?

Has this group ended?

Walkthrough - 228.Summary Range:

```
State Variable 1:
Name: read
Responsibility: Scan forward and detect whether the current number continues the range.
Moves when: Every iteration.

State Variable 2:
Name: write
Responsibility: Index where the current range began.
Moves when: The current range ends and a new range begins.

phase 1:
start = 0

for read in range(1, len(nums)):
    if nums[read] != nums[read - 1] + 1:
        # The range from start to read - 1 has ended
        if start == read - 1:
            result.append(str(nums[start]))
        else:
            result.append(f"{nums[start]}->{nums[read - 1]}")
        start = read

phase 2: Flush the final unfinished range

    if start == len(nums) - 1:
        result.append(str(nums[start]))
    else:
        result.append(f"{nums[start]}->{nums[-1]}")
```

## Pattern 3 — Two Readers + One Writer

Purpose:

Combine information from two sources.

Examples:

- Merge Sorted Array

# p1

largest unused value in nums1

# p2

largest unused value in nums2

# write

next safe position to write

##

Workshop 1

What is a pointer really?

Not syntax.

Responsibility.

We’ll solve:

- Move Zeroes
- Remove Element

⸻

Workshop 2

Pointer invariants.

We’ll solve:

- Summary Ranges
- Merge Sorted Array

⸻

Workshop 3

Read pointer vs Write pointer.

Classic interview pattern.

Examples:

- Remove Duplicates
- Move Zeroes
- Sort Colors (preview)

⸻

Workshop 4

Opposite-direction pointers.

Examples:

- Two Sum II
- Valid Palindrome
- Container With Most Water

⸻

Workshop 5

Fast/Slow pointers.

Examples:

- Linked List Cycle
- Middle of Linked List

Even though these are linked lists, the underlying thinking is transferable.

## Suggested progression next:

1. Remove Duplicates II
2. Sort Colors
3. Squares of a Sorted Array
4. Dutch National Flag
5. Interval Insert
6. Meeting Rooms
7. Container With Most Water

## Homework

```
1. What is the valid answer?


2. What information must never be overwritten?


3. What information is safe to overwrite?


4. State variables

read =

write =

start =

...


5. Invariant

"Everything before write..."
or
"start always..."


6. Only now write code.
```
