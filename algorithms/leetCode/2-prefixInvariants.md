# Prefix Invariants

**Purpose**

Learn the simplest and most common invariant family.

This family appears everywhere:

- Running maximum
- Running minimum
- Running sum
- Running count
- Running average
- First occurrence
- Last occurrence
- Any?
- All?

Later, you'll discover that many "easy" LeetCode problems are actually the same idea.

```
Invariant:
State stores the answer for the processed prefix.

Transition:
Ask: "What event changes that answer?"

Code:
Update the state only when that event occurs.
```

## What is a Prefix?

Imagine the array is a ruler.

Initially:

```
|------------------------|
^
processed
```

After one element:

```
[7 | 2 9 5 1]
```

After three:

```
[7 2 9 | 5 1]
```

After five:

```
[7 2 9 5 1 | ]
```

The processed region always grows from the left.

Engineers call this region:

**The processed prefix**

The remaining part is:

**The unprocessed suffix**

Why "Prefix"?

Because in computer science:

```
abcdef
```

Prefixes are:

```
a
ab
abc
abcd
abcde
abcdef
```

Always from the beginning.

Arrays are the same.

## The first invariant family

Now something beautiful happens.

Suppose the problem is:

```
Find the maximum.
```

We already know the invariant:

```
largest stores the largest value examined so far.
```

Let's replace:

```
examined so far
```

with

```
processed prefix
```

Now it becomes:

```
largest stores the largest value in the processed prefix.
```

That's exactly the same idea—but with a more precise term.

## The magic sentence

I want you to remember this sentence for the entire module:

> Answer state = correct answer for the processed prefix.

This is the defining characteristic of a Prefix Invariant.

Examples:

Maximum:

```
largest = maximum of processed prefix
```

Sum:

```
sum = sum of processed prefix
```

Count positives:

```
count = number of positive values in processed prefix
```

Contains duplicate:

```
exists = whether processed prefix contains a duplicate
```

## Running Extremum

Used for:

- Maximum
- Minimum

Template:

```
Problem
Find an extremum
↓
Information
Current extremum
↓
State
best
↓
Invariant
best stores the extremum of the processed prefix
↓
Transition
If the current element is a better extremum, update best.
```

| Step         | Minimum                      | Maximum                      |
| ------------ | ---------------------------- | ---------------------------- |
| Final Answer | Minimum value                | Maximum value                |
| Information  | Minimum value                | Maximum value                |
| State        | Current extremum             | Current extremum             |
| Invariant    | Extremum of processed prefix | Extremum of processed prefix |
| Transition   | Current < minimum            | Current > maximum            |

## Running Accumulation

Examples:

- Sum
- Count
- Conditional accumulation
- Average (later)

Template:

Count

```
Problem:
ccount the number of positive numbers
↓
Information:
the number of positive numbers in an array
↓
State:
count
↓
Invariant:
count stores the number of positive numbers of the processed prefix
↓
Transition:
count +1 if current element is a positive number, otherwise do nothing.
```

Sum

```
Problem:
sum positive numbers

Information:
sum of positive numbers

State:
total

Invariant:
total stores sum of positive numbers in processed prefix

Transition:
current > 0 → total += current
otherwise   → no change
```

The mental model is:

Process the prefix while accumulating contributions from qualifying elements.

Ask two questions:

1. Who contributes?

2. What do they contribute?

For example:

| Problem         | Who contributes? | Contribution |
| --------------- | ---------------- | -----------: |
| Count positives | Positive numbers |          `1` |
| Sum positives   | Positive numbers |    `current` |
| Sum evens       | Even numbers     |    `current` |
| Count > 10      | Numbers > 10     |          `1` |

## First / Last Occurrence

Examples:

- First negative
- Last negative
- First duplicate

First negative:

```
Information:
The first negative number in the array, if one exists.

State:
result = None
None means no negative has been found in the processed prefix

Invariant:
result stores the first negative number in the processed prefix, or None if no negative has been found.

Transition:
current is negative
AND
no negative has previously been found

if current < 0 and result is None:
    result = current
```

Last negative:

```
Invariant:
result stores the last negative number in the processed prefix, or None if no negative has been found.

Transition:
if current < 0:
    result = current
```

The core pattern:

```
First occurrence:
first match changes state
later matches do nothing

Last occurrence:
every match changes state
```

## Boolean Properties

Examples:

- Any positive?
- All positive?
- Contains duplicate?

Any negative

```
Information:
whether a negative number exists in the array

State:
exists = False

Invariant:
exists is True exactly when at least one negative number exists in the processed prefix.

Transition:
if current < 0:
    exists = True
```

All positive

```
Information:
whether all numbers are positive in the array

State:
all_positive = True

Invariant:
all_positive is True exactly when every element in the processed prefix is positive.

Transition:
if current <= 0:
    all_positive = False
```

The common invariant underneath all of them is:

**State stores the correct answer for the processed prefix.**
